package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;

public class AddSellItemEvent extends SellItemEvent {
    private final User user;

    public AddSellItemEvent(SellItem sellItem, User user) {
        super(sellItem);
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
