package org.by1337.bauction.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;
import org.jetbrains.annotations.NotNull;

/**
 * This event is triggered when a player successfully removes an item from sale.
 *
 * <p>This event may be asynchronous.</p>
 */
public class EventTakeItem extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final User user;
    @NotNull
    private final SellItem sellItem;

    /**
     * Constructs a EventSellItem with the specified user and item that has been successfully listed for sale.
     *
     * @param user     The player who successfully listed the item for sale.
     * @param sellItem The item that has been successfully listed for sale.
     */
    public EventTakeItem(@NotNull User user, @NotNull SellItem sellItem) {
        this.user = user;
        this.sellItem = sellItem;
    }

    /**
     * Constructs a EventSellItem with the specified user, item, and asynchronous flag.
     *
     * @param isAsync  – true indicates the event will fire asynchronously
     * @param user     The player who successfully listed the item for sale.
     * @param sellItem The item that has been successfully listed for sale.
     */
    public EventTakeItem(boolean isAsync, @NotNull User user, @NotNull SellItem sellItem) {
        super(isAsync);
        this.user = user;
        this.sellItem = sellItem;
    }

    /**
     * Gets the player who successfully listed the item for sale.
     *
     * @return The user who listed the item for sale.
     */
    @NotNull
    public User getUser() {
        return user;
    }

    /**
     * Gets the item that has been successfully listed for sale.
     *
     * @return The item that has been listed for sale.
     */
    @NotNull
    public SellItem getSellItem() {
        return sellItem;
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
