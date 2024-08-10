package org.by1337.bauction.db.action;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Action {
    private final ActionType type;
    @Nullable
    private final UUID owner;
    @Nullable
    private final long item;
    private final UUID server;

    public Action(ActionType type, @Nullable UUID owner, @Nullable long item, UUID server) {
        this.type = type;
        this.owner = owner;
        this.item = item;
        this.server = server;
    }

    public ActionType getType() {
        return type;
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    @Nullable
    public long getItem() {
        return item;
    }

    public UUID getServer() {
        return server;
    }

    public String toSql(String table) {
       /* return String.format("INSERT INTO %s (time, type, owner, server, uuid)" +
                "VALUES(%s, '%s', '%s', '%s', '%s')", table, System.currentTimeMillis(), type.name(), owner, server, item);*/
        return null;
    }

    public static Action fromResultSet(ResultSet resultSet) throws SQLException {
       /* ActionType type = ActionType.valueOf(resultSet.getString("type"));
        String owner = resultSet.getString("owner");
        String uuid = resultSet.getString("uuid");
        return new Action(
                type,
                "null".equals(owner) ? null : UUID.fromString(owner),
                "null".equals(uuid) ? null : new CUniqueName(uuid),
                null
        );*/
        return null;
    }

    @Override
    public String toString() {
        return "Action{" +
                "type=" + type +
                ", owner=" + owner +
                ", item=" + item +
                '}';
    }
}
