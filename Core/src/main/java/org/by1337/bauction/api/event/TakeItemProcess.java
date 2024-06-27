package org.by1337.bauction.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event is triggered when a player attempts to remove an item from sale.
 *
 * <p>This event may be asynchronous.</p>
 */
public class TakeItemProcess extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    @NotNull
    private final User user;
    @NotNull
    private final SellItem sellItem;
    @Nullable
    private String reason;

    /**
     * Constructs a TakeItemProcess with the specified user and item to be removed from sale.
     *
     * @param user The player attempting to remove the item from sale.
     * @param sellItem The item to be removed from sale.
     */
    public TakeItemProcess(@NotNull User user, @NotNull SellItem sellItem) {
        this.user = user;
        this.sellItem = sellItem;
    }

    /**
     * Constructs a TakeItemProcess with the specified user, item, and asynchronous flag.
     *
     * @param isAsync – true indicates the event will fire asynchronously
     * @param user The player attempting to remove the item from sale.
     * @param sellItem The item to be removed from sale.
     */
    public TakeItemProcess(boolean isAsync, @NotNull User user, @NotNull SellItem sellItem) {
        super(isAsync);
        this.user = user;
        this.sellItem = sellItem;
    }

    /**
     * Gets the player attempting to remove the item from sale.
     *
     * @return The user attempting to remove the item from sale.
     */
    @NotNull
    public User getUser() {
        return user;
    }

    /**
     * Gets the item to be removed from sale.
     *
     * @return The item to be removed from sale.
     */
    @NotNull
    public SellItem getSellItem() {
        return sellItem;
    }

    /**
     * Gets the reason why the item cannot be removed from sale.
     * This can be null, but if the event is cancelled, the player will see this message.
     * If the message is null, 'null' will be displayed in the chat to the player.
     * This message supports hex colors in the format &#rrggbb and also supports line breaks with \n,
     * including escaped line breaks with \\n.
     *
     * @return The reason why the item cannot be removed from sale.
     */
    @Nullable
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason why the item cannot be removed from sale.
     * This can be null, but if the event is cancelled, the player will see this message.
     * If the message is null, 'null' will be displayed in the chat to the player.
     * This message supports hex colors in the format &#rrggbb and also supports line breaks with \n,
     * including escaped line breaks with \\n.
     *
     * @param reason The reason why the item cannot be removed from sale.
     */
    public void setReason(@Nullable String reason) {
        this.reason = reason;
    }

    /**
     * Checks whether the event has been cancelled.
     *
     * @return True if the event has been cancelled, false otherwise.
     */
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    /**
     * Sets whether to cancel the event.
     *
     * @param cancel True to cancel the event, false to allow it.
     */
    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    /**
     * Gets the list of event handlers for this event.
     *
     * @return The list of event handlers for this event.
     */
    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the handler list for this event.
     *
     * @return The handler list for this event.
     */
    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
