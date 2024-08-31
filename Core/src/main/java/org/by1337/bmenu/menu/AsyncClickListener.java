package org.by1337.bmenu.menu;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.inventory.Inventory;
import org.by1337.bauction.Main;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.bmenu.BMenuApi;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Abstract class representing an asynchronous click listener for inventories.
 */
public abstract class AsyncClickListener extends Placeholder implements Listener {
    /**
     * The inventory associated with this click listener.
     */
    @Nullable
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
            executor = BMenuApi.getExecutor();
            runManager = executor::execute;
        } else {
            runManager = Runnable::run;
        }
    }

    protected void createInventory(int size, Component title, InventoryType type) {
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
        if (Objects.equals(e.getInventory(), inventory)) {
            onClose(e);
            close();
        }
    }


    public void close() {
        HandlerList.unregisterAll(this);
        if (executor != null) {
            executor.shutdown();
            executor = null;
        }
    }

    /**
     * Event handler for InventoryClickEvent.
     *
     * @param e The InventoryClickEvent.
     */
    @EventHandler
    public void onClick0(InventoryClickEvent e) {
        doClick(e);
    }

    /**
     * Event handler for InventoryDragEvent.
     *
     * @param e The InventoryDragEvent.
     */
    @EventHandler
    public void onClick0(InventoryDragEvent e) {
        doClick(e);
    }

    private void doClick(InventoryInteractEvent e) {
        if (Objects.equals(e.getInventory(), inventory)) {
            e.setCancelled(true);
            if ((System.currentTimeMillis() - lastClick) < 50) {
                return;
            } else {
                lastClick = System.currentTimeMillis() + 50;
            }
            runManager.run(() -> {
                try {
                    if (e instanceof InventoryDragEvent dragEvent) {
                        onClick(dragEvent);
                    } else if (e instanceof InventoryClickEvent clickEvent) {
                        onClick(clickEvent);
                    }
                } catch (Throwable t) {
                    Main.getMessage().error("An error occurred while processing the click!", t);
                }
            });
        }
    }

    /**
     * Re-registers the click listener with the Bukkit plugin manager.
     */
    protected void reRegister() {
        Bukkit.getPluginManager().registerEvents(this, BMenuApi.getInstance());
        if (async) {
            executor = BMenuApi.getExecutor();
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

    public @Nullable Inventory getInventory() {
        return inventory;
    }

    @FunctionalInterface
    private interface RunManager {
        void run(Runnable runnable);
    }

    @Override
    public String toString() {
        return "AsyncClickListener{" +
               "inventory=" + inventory +
               ", viewer=" + viewer +
               ", lastClick=" + lastClick +
               ", executor=" + executor +
               ", runManager=" + runManager +
               ", async=" + async +
               '}';
    }
}
