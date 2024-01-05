package org.by1337.bauction.db.action;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class ActionGiveMoney {
    private final String server;
    private final UUID uuid;
    private final Double count;
    public ActionGiveMoney(String server, UUID uuid, Double count) {
        this.server = server;
        this.uuid = uuid;
        this.count = count;
    }

    public String toSql(String table) {
        return String.format("INSERT INTO %s (server, uuid, count)" +
                "VALUES('%s', '%s', %s)", table, server, uuid, count);
    }

    public static ActionGiveMoney fromResultSet(ResultSet resultSet, boolean hasServer) throws SQLException {
        String server;
        if (hasServer)
            server = resultSet.getString("server");
        else server = "this";
        String uuid = resultSet.getString("uuid");
        Double count = resultSet.getDouble("count");
        return new ActionGiveMoney(
                server, UUID.fromString(uuid), count
        );
    }

    public static ActionGiveMoney fromResultSet(ResultSet resultSet) throws SQLException {
        return fromResultSet(resultSet, true);
    }

    @Override
    public String toString() {
        return "ActionGiveMoney{" +
                "server='" + server + '\'' +
                ", uuid=" + uuid +
                ", count=" + count +
                '}';
    }

    public String getServer() {
        return server;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Double getCount() {
        return count;
    }
}
