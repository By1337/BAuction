package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.PluginUnsoldItem;
import org.by1337.bauction.db.kernel.PluginUser;

public class TakeUnsoldItemEvent extends UnsoldItemEvent {
    private final PluginUser taker;


    public TakeUnsoldItemEvent(PluginUser taker, PluginUnsoldItem unsoldItem) {
        super(unsoldItem);
        this.taker = taker;
    }

    public PluginUser getTaker() {
        return taker;
    }
}
