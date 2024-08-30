package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.SellItem;

public class RemoveSellItemEvent extends SellItemEvent {

    public RemoveSellItemEvent(SellItem sellItem) {
        super(sellItem);
    }
}
