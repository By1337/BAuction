package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;

public class BuyItemEvent extends SellItemEvent{
    private final User buyer;

    public BuyItemEvent(User buyer, SellItem item) {
        super(item);
        this.buyer = buyer;
    }

    public User getBuyer() {
        return buyer;
    }
}
