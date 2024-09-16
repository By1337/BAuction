package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.common.db.event.Event;
import org.by1337.bauction.db.kernel.SellItem;

public class SellItemEvent extends Event {
    protected final SellItem sellItem;

    public SellItemEvent(SellItem sellItem) {
        this.sellItem = sellItem;
    }

    public SellItem getSellItem() {
        return sellItem;
    }
}
