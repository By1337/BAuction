package org.by1337.bauction.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;
import org.jetbrains.annotations.NotNull;

public class EventBuyItem extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final User buyer;
    @NotNull
    private final SellItem sellItem;

    public EventBuyItem(@NotNull User buyer, @NotNull SellItem sellItem) {
        this.buyer = buyer;
        this.sellItem = sellItem;
    }

    public EventBuyItem(boolean isAsync, @NotNull User buyer, @NotNull SellItem sellItem) {
        super(isAsync);
        this.buyer = buyer;
        this.sellItem = sellItem;
    }

    @NotNull
    public User getBuyer() {
        return buyer;
    }


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

