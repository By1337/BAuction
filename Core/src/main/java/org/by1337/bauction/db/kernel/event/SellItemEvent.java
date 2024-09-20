package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.common.db.event.Event;
import org.by1337.bauction.db.kernel.PluginSellItem;

public class SellItemEvent extends Event {
    protected final PluginSellItem sellItem;

    public SellItemEvent(PluginSellItem sellItem) {
        this.sellItem = sellItem;
    }

    public PluginSellItem getSellItem() {
        return sellItem;
    }
}
