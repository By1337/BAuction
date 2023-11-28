package org.by1337.bauction.db.action;

import org.by1337.bauction.db.kernel.CUnsoldItem;

import javax.annotation.Nullable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class Action {
    private final ActionType type;
    @Nullable
    private final UUID owner;
    @Nullable
    private final UUID item;

    public Action(ActionType type, @Nullable UUID owner, @Nullable UUID item) {
        this.type = type;
        this.owner = owner;
        this.item = item;
    }

    public ActionType getType() {
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

    public String toSql(String table){
        return String.format("INSERT INTO %s (time, type, owner, uuid)" +
                "VALUES(%s, '%s', '%s', '%s')", table, System.currentTimeMillis(), type.name(), owner, item);
    }

    public static Action fromResultSet(ResultSet resultSet) throws SQLException {
        ActionType type = ActionType.valueOf(resultSet.getString("type"));
        String owner = resultSet.getString("owner");
        String uuid = resultSet.getString("uuid");

        return new Action(
                type,
                "null".equals(owner) ? null : UUID.fromString(owner),
                "null".equals(uuid) ? null : UUID.fromString(uuid)
        );
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
