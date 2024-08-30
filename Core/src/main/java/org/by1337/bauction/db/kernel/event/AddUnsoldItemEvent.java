package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.UnsoldItem;

public class AddUnsoldItemEvent extends UnsoldItemEvent {
    public AddUnsoldItemEvent(UnsoldItem unsoldItem) {
        super(unsoldItem);
    }
}
