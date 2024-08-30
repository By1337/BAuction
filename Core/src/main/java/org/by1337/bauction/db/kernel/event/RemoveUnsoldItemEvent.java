package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.UnsoldItem;

public class RemoveUnsoldItemEvent extends UnsoldItemEvent {
    public RemoveUnsoldItemEvent(UnsoldItem unsoldItem) {
        super(unsoldItem);
    }
}
