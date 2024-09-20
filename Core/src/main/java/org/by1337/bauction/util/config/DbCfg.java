package org.by1337.bauction.util.config;

import org.by1337.blib.configuration.YamlContext;

import java.util.Objects;

public class DbCfg {
    private DbType dbType;
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
        lastSeed = context.getAsInteger("name-generator.last-seed");

        isHead = context.getAsBoolean("mysql-settings.is-head");
        host = context.getAsString("mysql-settings.host");
        dbName = context.getAsString("mysql-settings.db-name");
        user = context.getAsString("mysql-settings.user");
        password = context.getAsString("mysql-settings.password");
        port = context.getAsInteger("mysql-settings.port");
    }

    public void validate() {
        Objects.requireNonNull(host, "missing mysql-settings.host");
        Objects.requireNonNull(dbName, "missing mysql-settings.db-name");
        Objects.requireNonNull(user, "missing mysql-settings.user");
        Objects.requireNonNull(password, "missing mysql-settings.password");
    }


    public DbType getDbType() {
        return dbType;
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

    public enum DbType {
        MYSQL,
        FILE
    }
    public YamlContext getContext() {
        return context;
    }
}
