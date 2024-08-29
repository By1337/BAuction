package org.by1337.bauction.db.v2;

import org.by1337.bauction.db.kernel.UnsoldItem;
import org.by1337.bauction.db.kernel.User;

public class TakeUnsoldItemEvent extends Event {
    private final User taker;
    private final UnsoldItem unsoldItem;

    public TakeUnsoldItemEvent(User taker, UnsoldItem unsoldItem) {
        this.taker = taker;
        this.unsoldItem = unsoldItem;
    }

    public User getTaker() {
        return taker;
    }

    public UnsoldItem getUnsoldItem() {
        return unsoldItem;
    }
}
