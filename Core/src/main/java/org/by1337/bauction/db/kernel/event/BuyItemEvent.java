package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.PluginUser;

public class BuyItemEvent extends SellItemEvent{
    private final PluginUser buyer;

    public BuyItemEvent(PluginUser buyer, PluginSellItem item) {
        super(item);
        this.buyer = buyer;
    }

    public PluginUser getBuyer() {
        return buyer;
    }
}
