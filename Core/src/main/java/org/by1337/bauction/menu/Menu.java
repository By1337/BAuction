package org.by1337.bauction.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.requirement.Requirements;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Menu extends AsyncClickListener implements Placeholderable {

    private final List<CustomItemStack> items;
    protected List<CustomItemStack> customItemStacks = new LinkedList<>();
    protected final String title;
    protected final int size;
    protected final int updateInterval;
    protected Requirements openRequirements;
    protected List<Placeholderable> customPlaceHolders = new ArrayList<>();


    public Menu(MenuSetting setting, Player player) {
        this(setting.getItems(), setting.getTitle(), setting.getSize(), setting.getUpdateInterval(), setting.getViewRequirement(), player, setting.getType());
    }

    public Menu(List<CustomItemStack> items, String title, int size, int updateInterval, @Nullable Requirements viewRequirement, Player player, InventoryType type) {
        super(player, size, title, type);
        openRequirements = viewRequirement;
        this.items = items;
        this.title = title;
        this.size = size;
        this.updateInterval = updateInterval;
    }

    public void open() {
        Menu menu = this;
        syncUtil(() -> { // CancelledPacketHandleException bypass
            if (openRequirements != null) {
                if (openRequirements.check(menu, menu)) {
                    viewer.openInventory(inventory);
                    generate0();
                } else {
                    openRequirements.runDenyCommands(menu, menu);
                }
            } else {
                viewer.openInventory(inventory);
                generate0();
            }
        });
    }

    protected abstract void generate();

    protected void generate0() {
        inventory.clear();
        generate();
        LinkedList<CustomItemStack> list = new LinkedList<>();
        list.addAll(items);
        list.addAll(customItemStacks);
        for (CustomItemStack customItemStack : list) {
            if (customItemStack.getViewRequirement() == null || customItemStack.getViewRequirement().check(this, this)) {
                for (int slot : customItemStack.getSlots()) {
                    ItemStack item = customItemStack.getItem(this, this);
                    inventory.setItem(slot, item);
                }
            }
        }
        sendFakeTitle(replace(title));
    }

    public abstract void runCommand(Placeholderable holder, String... commands);

    @Override
    protected void onClick(InventoryDragEvent e) {
    }

    @Override
    public void onClick(InventoryClickEvent e) {
        if (e.getCurrentItem() == null) {
            return;
        }

        ItemStack itemStack = e.getCurrentItem();
        ItemMeta im = itemStack.getItemMeta();
        if (!im.getPersistentDataContainer().has(CustomItemStack.MENU_ITEM_KEY, PersistentDataType.INTEGER)) {
            inventory.clear();
            generate0();
            return;
        }
        int id = im.getPersistentDataContainer().get(CustomItemStack.MENU_ITEM_KEY, PersistentDataType.INTEGER);

        CustomItemStack customItemStack = getItemById(id);

        if (customItemStack == null) {
            inventory.clear();
            generate0();
            return;
        }
        customItemStack.run(e, this);
    }


    public void onClose(InventoryCloseEvent e) {
        new BukkitRunnable() {
            final Player player = (Player) e.getPlayer();

            @Override
            public void run() {
                player.updateInventory();
                for (ItemStack itemStack : player.getInventory()) {
                    if (itemStack == null) continue;
                    ItemMeta im = itemStack.getItemMeta();
                    if (im == null) continue;
                    if (im.getPersistentDataContainer().has(CustomItemStack.MENU_ITEM_KEY, PersistentDataType.INTEGER)) {
                        player.getInventory().remove(itemStack);
                    }
                }
            }
        }.runTaskLater(Main.getInstance(), 10);
    }


    @Nullable
    private CustomItemStack getItemById(int id) {
        LinkedList<CustomItemStack> list = new LinkedList<>();
        list.addAll(items);
        list.addAll(customItemStacks);
        for (CustomItemStack item : list) {
            if (item.getId() == id) {
                return item;
            }
        }
        return null;
    }

    public static List<ItemStack> giveItems(Player player, ItemStack... itemStack) {
        return new ArrayList<>(player.getInventory().addItem(itemStack).values());
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return viewer;
    }

    public void registerPlaceholderable(Placeholderable customPlaceHolder) {
        customPlaceHolders.add(customPlaceHolder);
    }
    abstract public void reopen();
}