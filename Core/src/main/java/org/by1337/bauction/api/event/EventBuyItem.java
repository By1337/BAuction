package org.by1337.bauction.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.PluginUser;
import org.jetbrains.annotations.NotNull;

public class EventBuyItem extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final PluginUser buyer;
    @NotNull
    private final PluginSellItem sellItem;

    public EventBuyItem(@NotNull PluginUser buyer, @NotNull PluginSellItem sellItem) {
        this.buyer = buyer;
        this.sellItem = sellItem;
    }

    public EventBuyItem(boolean isAsync, @NotNull PluginUser buyer, @NotNull PluginSellItem sellItem) {
        super(isAsync);
        this.buyer = buyer;
        this.sellItem = sellItem;
    }

    @NotNull
    public PluginUser getBuyer() {
        return buyer;
    }


    @NotNull
    public PluginSellItem getSellItem() {
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

