package org.by1337.bauction.db.v2;

import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;

public class BuyCountItemEvent extends Event{
    private final User buyer;
    private final SellItem item;
    private final int count;

    public BuyCountItemEvent(User buyer, SellItem item, int count) {
        this.buyer = buyer;
        this.item = item;
        this.count = count;
    }

    public User getBuyer() {
        return buyer;
    }

    public SellItem getItem() {
        return item;
    }

    public int getCount() {
        return count;
    }
}
