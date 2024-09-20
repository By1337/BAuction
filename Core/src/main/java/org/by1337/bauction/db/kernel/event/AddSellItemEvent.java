package org.by1337.bauction.db.kernel.event;

import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.PluginUser;

public class AddSellItemEvent extends SellItemEvent {
    private final PluginUser user;

    public AddSellItemEvent(PluginSellItem sellItem, PluginUser user) {
        super(sellItem);
        this.user = user;
    }

    public PluginUser getUser() {
        return user;
    }
}
