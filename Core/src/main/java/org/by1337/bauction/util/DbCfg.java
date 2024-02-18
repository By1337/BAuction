package org.by1337.bauction.util;

import org.by1337.blib.configuration.YamlContext;

import java.util.Objects;

public class DbCfg {
    private DbType dbType;
    private String serverId;
    private int lastSeed;
    private boolean isHead;
    private String host;
    private String dbName;
    private String user;
    private String password;
    private int port;
    private final YamlContext context;

    public DbCfg(YamlContext context) {
        this.context = context;
        load();
    }

    private void load() {
        dbType = context.getAsString("db-type").equals("file") ? DbType.FILE : DbType.MYSQL;
        serverId = Objects.requireNonNull(context.getAsString("server-id"), "missing server-id!");
        lastSeed = context.getAsInteger("name-generator.last-seed");

        isHead = Objects.requireNonNull(context.getAsBoolean( "mysql-settings.is-head"), "missing mysql-settings.is-head");
        host = Objects.requireNonNull(context.getAsString(    "mysql-settings.host"), "missing mysql-settings.host");
        dbName = Objects.requireNonNull(context.getAsString(  "mysql-settings.db-name"), "missing mysql-settings.db-name");
        user = Objects.requireNonNull(context.getAsString(    "mysql-settings.user"), "missing mysql-settings.user");
        password = Objects.requireNonNull(context.getAsString("mysql-settings.password"), "missing mysql-settings.password");
        port = Objects.requireNonNull(context.getAsInteger(   "mysql-settings.port"), "missing mysql-settings.port");

    }

    public DbType getDbType() {
        return dbType;
    }

    public String getServerId() {
        return serverId;
    }

    public int getLastSeed() {
        return lastSeed;
    }

    public boolean isHead() {
        return isHead;
    }

    public String getHost() {
        return host;
    }

    public String getDbName() {
        return dbName;
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public static enum DbType {
        MYSQL,
        FILE
    }

    public YamlContext getContext() {
        return context;
    }
}
