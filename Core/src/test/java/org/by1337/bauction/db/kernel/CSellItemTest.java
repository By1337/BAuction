package org.by1337.bauction.db.kernel;

import junit.framework.TestCase;
import org.bukkit.Material;
import org.by1337.bauction.BLibApi;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.util.CUniqueName;
import org.junit.Assert;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;

public class CSellItemTest extends TestCase {

    private CSellItem sellItem = new CSellItem(
            "item",
            "_By1337_",
            UUID.nameUUIDFromBytes("OfflinePlayer:_By1337_".getBytes()),
            125.0001,
            false,
            new HashSet<>(Arrays.asList("isblock", "grass_block")),
            100,
            200,
            new CUniqueName("key"),
            Material.GRASS_BLOCK,
            64,
            10.3,
            new HashSet<>(),
            null,
            "server-1"

    );

    public CSellItemTest() {
        BLibApi.setApi();
    }

    public void testParse() {
        SellItem sellItem1 = CSellItem.parse(sellItem);
        Assert.assertEquals(sellItem1, sellItem);
    }

    public void testFromBytes() throws IOException {
        SellItem sellItem1 = CSellItem.fromBytes(sellItem.getBytes());
        Assert.assertEquals(sellItem1, sellItem);
    }

    public void testFromResultSet() throws SQLException {
        SellItem sellItem1 = CSellItem.fromResultSet(createResultSet());
        Assert.assertEquals(sellItem1, sellItem);
    }

    private ResultSet createResultSet() throws SQLException {
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "uuid" -> sellItem.getUniqueName().getKey();
                case "seller_uuid" -> sellItem.getSellerUuid().toString();
                case "item" -> sellItem.getItem();
                case "seller_name" -> sellItem.getSellerName();
                case "tags" -> String.join(",", sellItem.getTags());
                case "sell_for" -> String.join(",", sellItem.sellFor);
                case "material" -> sellItem.material.name();
                case "server" -> sellItem.getServer();
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getString(Mockito.anyString());

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "price" -> sellItem.price;
                case "price_for_one" -> sellItem.priceForOne;
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getDouble(Mockito.anyString());

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "sale_by_the_piece" -> sellItem.saleByThePiece;
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getBoolean(Mockito.anyString());

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "time_listed_for_sale" -> sellItem.timeListedForSale;
                case "removal_date" -> sellItem.removalDate;
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getLong(Mockito.anyString());

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "amount" -> sellItem.amount;
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getInt(Mockito.anyString());
        return resultSet;
    }

}