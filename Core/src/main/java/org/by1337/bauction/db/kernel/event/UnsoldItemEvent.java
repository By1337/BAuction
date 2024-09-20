package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.common.db.event.Event;
import org.by1337.bauction.db.kernel.PluginUnsoldItem;

public class UnsoldItemEvent extends Event {
    protected final PluginUnsoldItem unsoldItem;

    public UnsoldItemEvent(PluginUnsoldItem unsoldItem) {
        this.unsoldItem = unsoldItem;
    }

    public PluginUnsoldItem getUnsoldItem() {
        return unsoldItem;
    }
}
