package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.PluginUser;

public class BuyCountItemEvent extends SellItemEvent{
    private final PluginUser buyer;
    private final int count;

    public BuyCountItemEvent(PluginUser buyer, PluginSellItem item, int count) {
        super(item);
        this.buyer = buyer;
        this.count = count;
    }

    public PluginUser getBuyer() {
        return buyer;
    }

    public int getCount() {
        return count;
    }
}
