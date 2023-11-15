package org.by1337.bauction.db.json.kernel;

import org.by1337.bauction.db.MemorySellItem;
import org.by1337.bauction.db.MemoryUser;

import java.util.UUID;

public class ActionType<T> {

    public final static ActionType<MemoryUser> UPDATE_MEMORY_USER = new ActionType<>(MemoryUser.class, "UPDATE_MEMORY_USER", false);
    public final static ActionType<MemorySellItem> UPDATE_MEMORY_SELL_ITEM = new ActionType<>(MemorySellItem.class, "UPDATE_MEMORY_SELL_ITEM", false);
    public final static ActionType<UUID> UPDATE_SELL_ITEM = new ActionType<>(UUID.class, "UPDATE_SELL_ITEM");
    public final static ActionType<UUID> REMOVE_USER = new ActionType<>(UUID.class, "REMOVE_USER");
    public final static ActionType<UUID> REMOVE_SELL_ITEM = new ActionType<>(UUID.class, "REMOVE_SELL_ITEM");
    public final static ActionType<UUID> UPDATE_USER = new ActionType<>(UUID.class, "UPDATE_USER");
    public final static ActionType<Void> SAVE_DB = new ActionType<>(Void.class, "SAVE_DB");

    private final Class<T> clazz;
    private final String name;
    private final boolean logged;

    ActionType(Class<T> clazz, String name) {
        this(clazz, name, true);
    }
    ActionType(Class<T> clazz, String name, boolean logged) {
        this.clazz = clazz;
        this.name = name;
        this.logged = logged;
    }

    public Class<T> getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public boolean isLogged() {
        return logged;
    }
}
