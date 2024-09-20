package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.PluginSellItem;

public class RemoveSellItemEvent extends SellItemEvent {

    public RemoveSellItemEvent(PluginSellItem sellItem) {
        super(sellItem);
    }
}
