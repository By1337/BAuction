package org.by1337.bauction.api.event;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.PluginUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuyItemProcess extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    @NotNull
    private final PluginUser buyer;
    @NotNull
    private final PluginSellItem sellItem;
    @Nullable
    private String reason;


    public BuyItemProcess(@NotNull PluginUser buyer, @NotNull PluginSellItem sellItem) {
        this.buyer = buyer;
        this.sellItem = sellItem;
    }

    public BuyItemProcess(boolean isAsync, @NotNull PluginUser buyer, @NotNull PluginSellItem sellItem) {
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