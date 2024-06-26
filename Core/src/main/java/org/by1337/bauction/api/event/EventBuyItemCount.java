package org.by1337.bauction.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;
import org.jetbrains.annotations.NotNull;

public class EventBuyItemCount extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final User buyer;
    @NotNull
    private final SellItem sellItem;
    private final int count;


    public EventBuyItemCount(@NotNull User buyer, @NotNull SellItem sellItem, int count) {
        this.buyer = buyer;
        this.sellItem = sellItem;
        this.count = count;
    }

    public EventBuyItemCount(boolean isAsync, @NotNull User buyer, @NotNull SellItem sellItem, int count) {
        super(isAsync);
        this.buyer = buyer;
        this.sellItem = sellItem;
        this.count = count;
    }


    @NotNull
    public User getBuyer() {
        return buyer;
    }


    @NotNull
    public SellItem getSellItem() {
        return sellItem;
    }

    public int getCount() {
        return count;
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

