package org.by1337.bauction.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.by1337.bauction.api.auc.UnsoldItem;
import org.by1337.bauction.api.auc.User;
import org.jetbrains.annotations.NotNull;

public class EventTakeUnsoldItem extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final User user;
    @NotNull
    private final UnsoldItem unsoldItem;

    public EventTakeUnsoldItem(@NotNull User user, @NotNull UnsoldItem unsoldItem) {
        this.user = user;
        this.unsoldItem = unsoldItem;
    }

    public EventTakeUnsoldItem(boolean isAsync, @NotNull User user, @NotNull UnsoldItem unsoldItem) {
        super(isAsync);
        this.user = user;
        this.unsoldItem = unsoldItem;
    }

    @NotNull
    public User getUser() {
        return user;
    }


    @NotNull
    public UnsoldItem getUnsoldItem() {
        return unsoldItem;
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

