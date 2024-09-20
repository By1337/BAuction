package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.PluginUnsoldItem;

public class RemoveUnsoldItemEvent extends UnsoldItemEvent {
    public RemoveUnsoldItemEvent(PluginUnsoldItem unsoldItem) {
        super(unsoldItem);
    }
}
