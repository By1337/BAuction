package org.by1337.bauction.datafix.db.mysql;

import org.by1337.bauction.Main;
import org.by1337.bauction.util.DbCfg;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySqlDBUpdater2 {
    public void update() {
        DbCfg dbCfg = Main.getDbCfg();
        if (!dbCfg.isHead()) {
            return;
        }
        if (dbCfg.getDbType() != DbCfg.DbType.MYSQL) return;
        Main.getMessage().logger("detected deprecated mysql db");


        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://" + dbCfg.getHost() + ":" + dbCfg.getPort() + "/" + dbCfg.getDbName() + "?useUnicode=true&characterEncoding=utf8&autoReconnect=true",
                dbCfg.getUser(), dbCfg.getPassword());
             Statement statement = connection.createStatement()
        ) {

            try {
                statement.executeUpdate("ALTER TABLE sell_items ADD COLUMN compressed BOOLEAN DEFAULT false");
            }catch (Throwable ignore){
            }
            statement.executeUpdate("ALTER TABLE unsold_items ADD COLUMN compressed BOOLEAN DEFAULT false");

        } catch (Exception e) {
            Main.getMessage().error("failed to update mysql db", e);
        }
    }
}
