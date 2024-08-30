package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;

public class BuyCountItemEvent extends SellItemEvent{
    private final User buyer;
    private final int count;

    public BuyCountItemEvent(User buyer, SellItem item, int count) {
        super(item);
        this.buyer = buyer;
        this.count = count;
    }

    public User getBuyer() {
        return buyer;
    }

    public int getCount() {
        return count;
    }
}
