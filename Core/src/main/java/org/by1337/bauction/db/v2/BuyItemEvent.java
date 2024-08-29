package org.by1337.bauction.db.v2;

import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;

public class BuyItemEvent extends Event{
    private final User buyer;
    private final SellItem item;

    public BuyItemEvent(User buyer, SellItem item) {
        this.buyer = buyer;
        this.item = item;
    }

    public User getBuyer() {
        return buyer;
    }

    public SellItem getItem() {
        return item;
    }
}
