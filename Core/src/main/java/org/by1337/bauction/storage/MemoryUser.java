package org.by1337.bauction.storage;

import org.by1337.bauction.SellItem;
import org.by1337.bauction.UserImpl;

import java.util.ArrayList;
import java.util.List;

public class MemoryUser {
    private final UserImpl handle;
    private List<SellItem> itemForSale = new ArrayList<>();

    public MemoryUser(UserImpl handle) {
        this.handle = handle;
    }

    public void addSellItem(SellItem sellItem){
        itemForSale.add(sellItem);
        handle.addSellItem(sellItem.getUuid());
    }

    public void removeSellItem(SellItem sellItem){
        itemForSale.remove(sellItem);
        handle.removeSellItem(sellItem.getUuid());
    }

}
