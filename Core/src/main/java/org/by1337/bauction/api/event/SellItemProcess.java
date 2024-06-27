package org.by1337.bauction.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * This event is triggered when a player attempts to list an item for sale.
 * At the time of this event, the plugin has not yet checked whether the player can list the item for sale,
 * for example, when the player has run out of available slots.
 * This event is useful for another plugin to cancel the listing of an item based on its own criteria.
 *
 * <p>This event may be asynchronous.</p>
 */
public class SellItemProcess extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    @NotNull
    private final User user;
    @NotNull
    private final SellItem sellItem;
    @Nullable
    private String reason;

    /**
     * Constructs a SellItemProcess event with the specified user and item to be listed for sale.
     *
     * @param user The player listing the item for sale.
     * @param sellItem The item being listed for sale.
     */
    public SellItemProcess(@NotNull User user, @NotNull SellItem sellItem) {
        this.user = user;
        this.sellItem = sellItem;
    }

    /**
     * Constructs a SellItemProcess event with the specified user and item to be listed for sale.
     *
     * @param user The player listing the item for sale.
     * @param sellItem The item being listed for sale.
     * @param isAsync – true indicates the event will fire asynchronously
     */
    public SellItemProcess(boolean isAsync, @NotNull User user, @NotNull SellItem sellItem) {
        super(isAsync);
        this.user = user;
        this.sellItem = sellItem;
    }

    /**
     * Gets the player listing the item for sale.
     *
     * @return The user listing the item for sale.
     */
    @NotNull
    public User getUser() {
        return user;
    }

    /**
     * Gets the item being listed for sale.
     *
     * @return The item being listed for sale.
     */
    @NotNull
    public SellItem getSellItem() {
        return sellItem;
    }

    /**
     * Gets the reason why the item cannot be listed for sale.
     * This can be null, but if the event is cancelled, the player will see this message.
     * If the message is null, 'null' will be displayed in the chat to the player.
     * This message supports hex colors in the format &#rrggbb and also supports line breaks with \n,
     * including escaped line breaks with \\n.
     *
     * @return The reason why the item cannot be listed for sale.
     */
    @Nullable
    public String getReason() {
        return reason;
    }

    /**
     * Sets the reason why the item cannot be listed for sale.
     * This can be null, but if the event is cancelled, the player will see this message.
     * If the message is null, 'null' will be displayed in the chat to the player.
     * This message supports hex colors in the format &#rrggbb and also supports line breaks with \n,
     * including escaped line breaks with \\n.
     *
     * @param reason The reason why the item cannot be listed for sale.
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