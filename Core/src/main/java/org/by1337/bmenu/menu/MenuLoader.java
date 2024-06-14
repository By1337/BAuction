package org.by1337.bmenu.menu;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.NameKey;
import org.by1337.bmenu.BMenuApi;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;

public class MenuLoader implements Closeable, Listener {
    private final Map<NameKey, MenuSetting> menus;
    private final Plugin plugin;
    private final File menuFolder;
    private final ResourceLeakDetectorMode resourceLeakDetectorMode;
    private final BukkitTask task;

    public MenuLoader(Plugin plugin, File menuFolder, ResourceLeakDetectorMode resourceLeakDetectorMode) {
        this.menuFolder = menuFolder;
        menus = new HashMap<>();
        this.plugin = plugin;
        this.resourceLeakDetectorMode = resourceLeakDetectorMode;
        if (resourceLeakDetectorMode.scanTime != -1) {
            task = Bukkit.getScheduler().runTaskTimer(plugin, this::resourceLeakDetectorTick, resourceLeakDetectorMode.scanTime, resourceLeakDetectorMode.scanTime);
        } else {
            task = null;
        }
    }

    public MenuLoader(Plugin plugin, File menuFolder) {
        this(plugin, menuFolder, ResourceLeakDetectorMode.DEFAULT);
    }

    public void load() {
        menus.clear();
        for (File file1 : getFiles(menuFolder)) {
            if (!file1.getName().endsWith(".yml")) continue;
            try {
                YamlConfiguration cfg = new YamlConfiguration();
                cfg.load(file1);
                YamlContext context = new YamlContext(cfg);
                MenuSetting setting = MenuFactory.create(context, this);
                menus.put(setting.getId(), setting);
            } catch (Throwable e) {
                BMenuApi.getMessage().error("Failed to load menu '%s'", e, file1.getPath());
            }
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    private List<File> getFiles(File folder) {
        List<File> files = new ArrayList<>();
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                files.addAll(getFiles(file));
            } else {
                files.add(file);
            }
        }
        return files;
    }

    private void resourceLeakDetectorTick() {
        for (RegisteredListener listener : InventoryCloseEvent.getHandlerList().getRegisteredListeners()) {
            if (listener.getListener() instanceof AsyncClickListener asyncClickListener) {
                boolean isDifferentInventory = !asyncClickListener.getViewer().getOpenInventory().getTopInventory().equals(asyncClickListener.getInventory());
                boolean isViewerOffline = !asyncClickListener.getViewer().isOnline();

                if (isDifferentInventory || isViewerOffline) {
                    BMenuApi.getMessage().error(
                            "[ResourceLeakDetector] Checking listener: " + asyncClickListener +
                            ", TopInventory: " + asyncClickListener.getViewer().getOpenInventory().getTopInventory() +
                            ", Listener Inventory: " + asyncClickListener.getInventory() +
                            ", Is Different Inventory: " + isDifferentInventory +
                            ", Is Viewer Offline: " + isViewerOffline
                    );
                    asyncClickListener.close();
                    asyncClickListener.getViewer().closeInventory(InventoryCloseEvent.Reason.PLUGIN);
                }
            }
        }
    }


    @EventHandler
    public void on(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player)
            removeAll(player, i -> i.hasItemMeta() && i.getItemMeta().getPersistentDataContainer().has(MenuItemBuilder.MENU_ITEM_KEY, PersistentDataType.INTEGER));
    }

    @EventHandler
    public void on(InventoryOpenEvent event) {
        if (event.getPlayer() instanceof Player player)
            removeAll(player, i -> i.hasItemMeta() && i.getItemMeta().getPersistentDataContainer().has(MenuItemBuilder.MENU_ITEM_KEY, PersistentDataType.INTEGER));
    }

    public void removeAll(Player player, Predicate<ItemStack> filter) {
        var inv = player.getInventory();
        for (int i = 0; i < inv.getSize(); i++) {
            var item = inv.getItem(i);
            if (item != null && filter.test(item)) {
                inv.setItem(i, null);
            }
        }
    }

    @Nullable
    public MenuSetting getMenu(String id) {
        return getMenu(new NameKey(id));
    }

    @Nullable
    public MenuSetting getMenu(NameKey id) {
        return menus.get(id);
    }

    public Set<NameKey> getMenus() {
        return menus.keySet();
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public File getMenuFolder() {
        return menuFolder;
    }

    @Override
    public void close() {
        if (task != null) {
            task.cancel();
        }
        HandlerList.unregisterAll(this);
    }

    public enum ResourceLeakDetectorMode {
        NONE(-1),
        DEFAULT(2_000),
        PANIC(200);

        private final long scanTime;

        ResourceLeakDetectorMode(long scanTime) {
            this.scanTime = scanTime;
        }
    }
}
