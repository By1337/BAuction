package org.by1337.bauction.db.json.kernel;

import org.by1337.bauction.db.MemorySellItem;
import org.by1337.bauction.db.MemoryUser;
import org.by1337.bauction.db.json.kernel.SellItem;
import org.by1337.bauction.db.json.kernel.User;

import java.util.UUID;

public enum ActionType {
    UPDATE_USER(User.class),
    UPDATE_MEMORY_USER(MemoryUser.class),
    UPDATE_SELL_ITEM(SellItem.class),
    UPDATE_MEMORY_SELL_ITEM(MemorySellItem.class),
    REMOVE_USER(UUID.class),
    REMOVE_SELL_ITEM(UUID.class);

    private final Class<?> clazz;

    ActionType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Class<?> getClazz() {
        return clazz;
    }
}
