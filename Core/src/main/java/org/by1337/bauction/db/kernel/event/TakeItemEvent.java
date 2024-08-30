package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;

public class TakeItemEvent extends SellItemEvent {
    private final User user;

    public TakeItemEvent(SellItem sellItem, User user) {
        super(sellItem);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
