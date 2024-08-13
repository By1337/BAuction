package org.by1337.bauction.db.v2;

import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;

public class TakeSellItemEvent extends Event {
    private final SellItem sellItem;
    private final User user;

    public TakeSellItemEvent(SellItem sellItem, User user) {
        this.sellItem = sellItem;
        this.user = user;
    }

    public SellItem getSellItem() {
        return sellItem;
    }

    public User getUser() {
        return user;
    }
}
