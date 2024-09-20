package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.PluginUnsoldItem;

public class AddUnsoldItemEvent extends UnsoldItemEvent {
    public AddUnsoldItemEvent(PluginUnsoldItem unsoldItem) {
        super(unsoldItem);
    }
}
