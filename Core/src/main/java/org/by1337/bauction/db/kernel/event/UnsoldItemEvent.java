package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.common.db.event.Event;
import org.by1337.bauction.db.kernel.UnsoldItem;

public class UnsoldItemEvent extends Event {
    protected final UnsoldItem unsoldItem;

    public UnsoldItemEvent(UnsoldItem unsoldItem) {
        this.unsoldItem = unsoldItem;
    }

    public UnsoldItem getUnsoldItem() {
        return unsoldItem;
    }
}
