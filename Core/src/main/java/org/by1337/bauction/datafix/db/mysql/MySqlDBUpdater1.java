package org.by1337.bauction.datafix.db.mysql;

import org.bukkit.Material;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.util.config.ConfigUtil;
import org.by1337.blib.configuration.YamlConfig;
import org.by1337.bauction.Main;
import org.by1337.bauction.util.id.CUniqueName;

import java.sql.*;
import java.util.*;

public class MySqlDBUpdater1 {

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
        );) {

            List<SellItem> sellItems = parseSellItems(connection);
            try (PreparedStatement stat = connection.prepareStatement("DROP TABLE IF EXISTS sell_items")) {
                stat.execute();
            }
            try (PreparedStatement stat = connection.prepareStatement("CREATE TABLE IF NOT EXISTS sell_items ( uuid VARBINARY(36) NOT NULL PRIMARY KEY, seller_uuid VARCHAR(36) NOT NULL, item TEXT NOT NULL, seller_name VARCHAR(50) NOT NULL, price DOUBLE NOT NULL, sale_by_the_piece BOOLEAN NOT NULL, tags TEXT NOT NULL, time_listed_for_sale BIGINT NOT NULL, removal_date BIGINT NOT NULL, material VARCHAR(50) NOT NULL, amount TINYINT NOT NULL, price_for_one DOUBLE NOT NULL, sell_for TEXT NOT NULL, server VARBINARY(36) NOT NULL )")) {
                stat.execute();
            }

            for (SellItem sellItem : sellItems) {
                try (PreparedStatement stat = connection.prepareStatement(sellItem.toSql("sell_items"))) {
                    stat.execute();
                }
            }

        } catch (Exception e) {
            Main.getMessage().error("failed to update mysql db", e);
        }
    }

    private List<SellItem> parseSellItems(Connection connection) {
        List<SellItem> sellItems = new ArrayList<>();

        String query = "SELECT * FROM sell_items";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            while (resultSet.next()) {
                sellItems.add(fromResultSet(resultSet));
            }
        } catch (SQLException e) {
            Main.getMessage().error(e);
        }
        return sellItems;
    }

    private static SellItem fromResultSet(ResultSet resultSet) throws SQLException {
        String server = Main.getServerId();
        if (server == null) server = "server-1";
        return SellItem.builder()
                .uniqueName(new CUniqueName(resultSet.getString("uuid")))
                .sellerUuid(UUID.fromString(resultSet.getString("seller_uuid")))
                .item(resultSet.getString("item"))
                .sellerName(resultSet.getString("seller_name"))
                .price(resultSet.getDouble("price"))
                .saleByThePiece(resultSet.getBoolean("sale_by_the_piece"))
                .tags(new HashSet<>(Arrays.asList(resultSet.getString("tags").split(","))))
                .timeListedForSale(resultSet.getLong("time_listed_for_sale"))
                .removalDate(resultSet.getLong("removal_date"))
                .material(Material.valueOf(resultSet.getString("material")))
                .amount(resultSet.getInt("amount"))
                .priceForOne(resultSet.getDouble("price_for_one"))
                .sellFor(new HashSet<>(Arrays.asList(resultSet.getString("sell_for").split(","))))
                .server(server)
                .build();
    }
}
