package org.by1337.bauction.datafix.db.mysql;

import org.by1337.bauction.Main;
import org.by1337.bauction.util.config.ConfigUtil;
import org.by1337.blib.configuration.YamlConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class MySqlDBUpdater2 {
    public void update() {
        YamlConfig cfg = ConfigUtil.load("dbCfg.yml");

        if (!cfg.getAsString("db-type").equals("mysql")) return;

        Main.getMessage().log("detected deprecated mysql db");


        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://" +
                        cfg.getAsString("mysql-settings.host") + ":" +
                        cfg.getAsString("mysql-settings.port") + "/" +
                        cfg.getAsString("mysql-settings.db-name") + "?useUnicode=true&characterEncoding=utf8&autoReconnect=true",
                cfg.getAsString("mysql-settings.user"),
                cfg.getAsString("mysql-settings.password")
        );
             Statement statement = connection.createStatement()
        ) {

            try {
                statement.executeUpdate("ALTER TABLE sell_items ADD COLUMN compressed BOOLEAN DEFAULT false");
            } catch (Throwable ignore) {
            }
            try {
                statement.executeUpdate("ALTER TABLE unsold_items ADD COLUMN compressed BOOLEAN DEFAULT false");
            } catch (Throwable ignore) {
            }


        } catch (Exception e) {
            Main.getMessage().error("failed to update mysql db", e);
        }
    }
}
