package org.by1337.bauction.menu;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.by1337.api.BLib;
import org.by1337.bauction.Main;

/**
 * Abstract class representing an asynchronous click listener for inventories.
 */
public abstract class AsyncClickListener implements Listener {
    /**
     * The inventory associated with this click listener.
     */
    protected final Inventory inventory;
    /**
     * The player viewing the inventory.
     */
    protected final Player viewer;
    /**
     * Cooldown time between clicks to prevent rapid clicking.
     */
    private long lastClick = 0;

    /**
     * Constructor for the AsyncClickListener.
     *
     * @param viewer The player viewing the inventory.
     * @param size   The size of the inventory.
     * @param title  The title of the inventory.
     */
    public AsyncClickListener(Player viewer, int size, String title) {
        this.viewer = viewer;
        inventory = Bukkit.createInventory(null, size, title);
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    /**
     * Abstract method to be implemented for handling inventory close events.
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
            HandlerList.unregisterAll(this);
        }
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
            new Thread(() -> onClick(e)).start();
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
            new Thread(() -> onClick(e)).start();
        }
    }

    /**
     * Re-registers the click listener with the Bukkit plugin manager.
     */
    protected void reRegister() {
        Bukkit.getPluginManager().registerEvents(this, Main.getInstance());
    }

    /**
     * Sends a fake title to the player viewing the inventory.
     *
     * @param title The title to be sent.
     */
    protected void sendFakeTitle(String title) {
        BLib.getApi().getFakeTitleFactory().get().send(inventory, title);
    }

    /**
     * Utility method for executing a Runnable task on the server's main thread with a delay of 0 ticks.
     * This ensures synchronization with the server's main thread.
     *
     * @param runnable The Runnable task to be executed.
     */
    protected void syncUtil(Runnable runnable) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), runnable, 0);
    }
}
