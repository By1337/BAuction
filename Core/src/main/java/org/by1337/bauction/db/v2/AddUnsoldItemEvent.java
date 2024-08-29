package org.by1337.bauction.db.v2;

import org.by1337.bauction.db.kernel.UnsoldItem;
import org.by1337.bauction.db.kernel.User;

public class AddUnsoldItemEvent extends Event {
    private final UnsoldItem unsoldItem;

    public AddUnsoldItemEvent(UnsoldItem unsoldItem) {
        this.unsoldItem = unsoldItem;
    }

    public UnsoldItem getUnsoldItem() {
        return unsoldItem;
    }
}
