package org.by1337.bauction.api.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.by1337.bauction.db.kernel.PluginUnsoldItem;
import org.by1337.bauction.db.kernel.PluginUser;
import org.jetbrains.annotations.NotNull;

public class EventTakeUnsoldItem extends Event {
    private static final HandlerList handlers = new HandlerList();

    @NotNull
    private final PluginUser user;
    @NotNull
    private final PluginUnsoldItem unsoldItem;

    public EventTakeUnsoldItem(@NotNull PluginUser user, @NotNull PluginUnsoldItem unsoldItem) {
        this.user = user;
        this.unsoldItem = unsoldItem;
    }

    public EventTakeUnsoldItem(boolean isAsync, @NotNull PluginUser user, @NotNull PluginUnsoldItem unsoldItem) {
        super(isAsync);
        this.user = user;
        this.unsoldItem = unsoldItem;
    }

    @NotNull
    public PluginUser getUser() {
        return user;
    }


    @NotNull
    public PluginUnsoldItem getUnsoldItem() {
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

