package org.by1337.bauction.db.action;

import javax.annotation.Nullable;
import java.util.UUID;

public class Action {
    private final DBActionType type;
    @Nullable
    private final UUID owner;
    @Nullable
    private final UUID item;

    public Action(DBActionType type, @Nullable UUID owner, @Nullable UUID item) {
        this.type = type;
        this.owner = owner;
        this.item = item;
    }

    public DBActionType getType() {
        return type;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    @Nullable
    public UUID getItem() {
        return item;
    }
}
