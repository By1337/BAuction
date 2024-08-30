package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.UnsoldItem;
import org.by1337.bauction.db.kernel.User;

public class TakeUnsoldItemEvent extends UnsoldItemEvent {
    private final User taker;


    public TakeUnsoldItemEvent(User taker, UnsoldItem unsoldItem) {
        super(unsoldItem);
        this.taker = taker;
    }

    public User getTaker() {
        return taker;
    }
}
