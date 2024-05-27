package org.by1337.bmenu.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.bmenu.BMenuApi;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Abstract class representing an asynchronous click listener for inventories.
 */
public abstract class AsyncClickListener extends Placeholder implements Listener {
    /**
     * The inventory associated with this click listener.
     */
    protected Inventory inventory;
    /**
     * The player viewing the inventory.
     */
    protected final Player viewer;
    /**
     * Cooldown time between clicks to prevent rapid clicking.
     */
    private long lastClick = 0;

    @Nullable
    private ExecutorService executor;

    private RunManager runManager;
    private final boolean async;

    /**
     * Constructor for the AsyncClickListener.
     *
     * @param viewer The player viewing the inventory.
     */
    public AsyncClickListener(Player viewer) {
        this(viewer, true);
    }

    public AsyncClickListener(Player viewer, boolean async) {
        this.viewer = viewer;
        this.async = async;
        Bukkit.getPluginManager().registerEvents(this, BMenuApi.getInstance());

        if (async) {
            executor = Executors.newSingleThreadExecutor();
            runManager = executor::execute;
        } else {
            runManager = Runnable::run;
        }
    }

    protected void createInventory(int size, String title, InventoryType type) {
        if (type == InventoryType.CHEST) {
            inventory = Bukkit.createInventory(null, size, title);
        } else {
            inventory = Bukkit.createInventory(null, type, title);
        }
    }

    /**
     * Abstract method to be implemented for handling inventory clearCommandMap events.
     *
     * @param e The InventoryCloseEvent.
     */
    protected abstract void onClose(InventoryCloseEvent e);

    /**
     * Abstract method to be implemented for handling inventory click events.
     *
     * @param e The InventoryClickEvent.
     */
    protected abstract void onClick(InventoryClickEvent e);

    /**
     * Abstract method to be implemented for handling inventory drag events.
     *
     * @param e The InventoryDragEvent.
     */
    protected abstract void onClick(InventoryDragEvent e);

    /**
     * Event handler for InventoryCloseEvent.
     *
     * @param e The InventoryCloseEvent.
     */
    @EventHandler
    public void onClose0(InventoryCloseEvent e) {
        if (inventory.equals(e.getInventory())) {
            onClose(e);
            close();
            syncUtil(() -> {
                viewer.updateInventory();
                for (ItemStack itemStack : viewer.getInventory()) {
                    if (itemStack == null) continue;
                    ItemMeta im = itemStack.getItemMeta();
                    if (im == null) continue;
                    if (im.getPersistentDataContainer().has(MenuItemBuilder.MENU_ITEM_KEY, PersistentDataType.INTEGER)) {
                        viewer.getInventory().remove(itemStack);
                    }
                }
            }, 10);
        }
    }


    public void close() {
        HandlerList.unregisterAll(this);
        if (executor != null)
            executor.shutdown();
    }

    /**
     * Event handler for InventoryClickEvent.
     *
     * @param e The InventoryClickEvent.
     */
    @EventHandler
    public void onClick0(InventoryClickEvent e) {
        if (inventory.equals(e.getInventory())) {
            e.setCancelled(true);
            if ((System.currentTimeMillis() - lastClick) < 50) {
                return;
            } else {
                lastClick = System.currentTimeMillis() + 50;
            }
            runManager.run(() -> onClick(e));
        }
    }

    /**
     * Event handler for InventoryDragEvent.
     *
     * @param e The InventoryDragEvent.
     */
    @EventHandler
    public void onClick0(InventoryDragEvent e) {
        if (inventory.equals(e.getInventory())) {
            e.setCancelled(true);
            if ((System.currentTimeMillis() - lastClick) < 50) {
                return;
            } else {
                lastClick = System.currentTimeMillis() + 50;
            }
            runManager.run(() -> onClick(e));
        }
    }

    /**
     * Re-registers the click listener with the Bukkit plugin manager.
     */
    protected void reRegister() {
        Bukkit.getPluginManager().registerEvents(this, BMenuApi.getInstance());
        if (async) {
            executor = Executors.newSingleThreadExecutor();
            runManager = executor::execute;
        } else {
            runManager = Runnable::run;
        }
    }

    /**
     * Sends a fake title to the player viewing the inventory.
     *
     * @param title The title to be sent.
     */
    protected void sendFakeTitle(String title) {
        BLib.getApi().getFakeTitleFactory().get().send(inventory, BMenuApi.getMessage().componentBuilder(title));
    }

    /**
     * Utility method for executing a Runnable task on the server's main thread with a delay of 0 ticks.
     * This ensures synchronization with the server's main thread.
     *
     * @param runnable The Runnable task to be executed.
     */
    public static void syncUtil(Runnable runnable) {
        syncUtil(runnable, 0);
    }

    public static void syncUtil(Runnable runnable, long delay) {
        Bukkit.getScheduler().runTaskLater(BMenuApi.getInstance(), runnable, delay);
    }

    public Player getViewer() {
        return viewer;
    }

    @FunctionalInterface
    private interface RunManager {
        void run(Runnable runnable);
    }
}
