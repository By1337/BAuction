package org.by1337.bauction.menu;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.api.BLib;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.requirement.Requirements;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public abstract class Menu implements Listener, Iterable<CustomItemStack>, Placeholderable {

    private LinkedList<CustomItemStack> items;
    protected LinkedList<CustomItemStack> customItemStacks = new LinkedList<>();
    protected final String title;
    protected final int size;
    protected final int updateInterval;
    protected Requirements openRequirements = null;
    protected final Inventory inventory;
    protected Player bukkitPlayer = null;
    protected final BukkitTask task;
    protected final FileConfiguration menuFile;

    protected List<Placeholderable> customPlaceHolders = new ArrayList<>();


    public Menu(MenuSetting setting) {
        this(setting.getItems(), setting.getTitle(), setting.getSize(), setting.getUpdateInterval(), setting.getMenuFile(), setting.getViewRequirement());
    }

    public Menu(LinkedList<CustomItemStack> items, String title, int size, int updateInterval, FileConfiguration menuFile, @Nullable Requirements viewRequirement) {
        openRequirements = viewRequirement;
        this.menuFile = menuFile;
        Collections.sort(items);
        this.items = items;
        this.title = title;
        this.size = size;
        this.updateInterval = updateInterval;
        inventory = Bukkit.createInventory(null, size, Main.getMessage().messageBuilder(replacePlaceholders(title)));
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());

        if (updateInterval != -1) {
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (getBukkitPlayer() != null) {
                        generate0();
                    }
                }
            }.runTaskTimer(Main.getInstance(), 0, updateInterval);
        } else {
            task = null;
        }
    }

    public void open() {
        Menu menu = this;
        if (openRequirements != null) {
            if (openRequirements.check(menu::replacePlaceholders, menu)) {
                if (bukkitPlayer == null) {
                    throw new IllegalArgumentException("Player is null!");
                } else {
                    bukkitPlayer.openInventory(inventory);
                    generate0();
                }
            } else {
                openRequirements.runDenyCommands(menu, menu::replacePlaceholders);
            }
        } else {
            if (bukkitPlayer == null) {
                throw new IllegalArgumentException("Player is null!");
            } else {
                bukkitPlayer.openInventory(inventory);
                generate0();
            }
        }

    }

    protected abstract void generate();

    protected void generate0() {
        inventory.clear();
        generate();
        LinkedList<CustomItemStack> list = new LinkedList<>();
        list.addAll(items);
        list.addAll(customItemStacks);
        for (CustomItemStack customItemStack : list) {
            if (customItemStack.getViewRequirement() == null || customItemStack.getViewRequirement().check(this::replacePlaceholders, this)) {
                for (int slot : customItemStack.getSlots()) {
                    customPlaceHolders.forEach(customItemStack::registerPlaceholder);
                    ItemStack item = customItemStack.getItem(this::replacePlaceholders, this);
                    inventory.setItem(slot, item);
                }
            }
        }
        BLib.getApi().getFakeTitleFactory().get().send(inventory, Main.getMessage().messageBuilder(replacePlaceholders(title)));
    }

    public abstract void runCommand(Placeholderable holder, String... commands);

    public String replacePlaceholders(String input) {
        input = replace(input);
        return Main.getMessage().messageBuilder(input, bukkitPlayer);
    }

    private long cd = 0;

    @EventHandler
    public void onClick(InventoryDragEvent e) {
        if (inventory.equals(e.getInventory())) {
            if ((System.currentTimeMillis() - cd) < 50) {
                e.setCancelled(true);
                return;
            } else {
                cd = System.currentTimeMillis() + 50;
            }

            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClick(InventoryClickEvent e) {
        if (inventory.equals(e.getInventory())) {
            if ((System.currentTimeMillis() - cd) < 50) {
                e.setCancelled(true);
                return;
            } else {
                cd = System.currentTimeMillis() + 50;
            }

            e.setCancelled(true);
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
    }

    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        if (inventory.equals(e.getInventory())) {
            close(e);
        }
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

    private void close(@Nullable InventoryCloseEvent e) {
        if (task != null) {
            task.cancel();
        }
        HandlerList.unregisterAll(this);
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

    @NotNull
    @Override
    public Iterator<CustomItemStack> iterator() {
        return items.iterator();
    }

    public void setOpenRequirements(Requirements openRequirements) {
        this.openRequirements = openRequirements;
    }

    public void addItem(CustomItemStack customItemStack) {
        items.add(customItemStack);
    }


    public LinkedList<CustomItemStack> getItems() {
        return this.items;
    }

    public String getTitle() {
        return this.title;
    }

    public int getSize() {
        return this.size;
    }

    public int getUpdateInterval() {
        return this.updateInterval;
    }

    public Requirements getOpenRequirements() {
        return this.openRequirements;
    }

    public Player getBukkitPlayer() {
        return this.bukkitPlayer;
    }

    public void setBukkitPlayer(Player bukkitPlayer) {
        this.bukkitPlayer = bukkitPlayer;
    }

    public BukkitTask getTask() {
        return this.task;
    }

    public List<Placeholderable> getCustomPlaceHolders() {
        return this.customPlaceHolders;
    }

    public void setItems(LinkedList<CustomItemStack> items) {
        this.items = items;
    }

    public void addCustomPlaceHolders(Placeholderable customPlaceHolder) {
        customPlaceHolders.add(customPlaceHolder);
    }

    public void setCustomPlaceHolders(List<Placeholderable> customPlaceHolders) {
        this.customPlaceHolders = customPlaceHolders;
    }

    public LinkedList<CustomItemStack> getCustomItemStacks() {
        return customItemStacks;
    }

    public void setCustomItemStacks(LinkedList<CustomItemStack> customItemStacks) {
        this.customItemStacks = customItemStacks;
    }
}