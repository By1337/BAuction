/*
package org.by1337.bauction.db.kernel;

import junit.framework.TestCase;
import org.by1337.bauction.BLibApi;
import org.by1337.bauction.util.id.CUniqueName;
import org.junit.Assert;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class UnsoldItemTest extends TestCase {

    private UnsoldItem unsoldItem = new UnsoldItem(
            "item",
            100,
            UUID.nameUUIDFromBytes("OfflinePlayer:_By1337_".getBytes()),
            new CUniqueName("key"),
            200,
            null,
            false
    );
    public UnsoldItemTest() {
        BLibApi.setApi();
    }

    public void testFromResultSet() throws SQLException {
        Assert.assertEquals(UnsoldItem.fromResultSet(createResultSet()), unsoldItem);
    }

    public void testGetBytes() throws IOException {
        Assert.assertEquals(unsoldItem, UnsoldItem.fromBytes(unsoldItem.getBytes()));
    }
    private ResultSet createResultSet() throws SQLException {
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "item" -> unsoldItem.item;
                case "seller_uuid" -> unsoldItem.getSellerUuid().toString();
                case "uuid" -> unsoldItem.uniqueName.getKey();
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getString(Mockito.anyString());

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "expired" -> unsoldItem.expired;
                case "delete_via" -> unsoldItem.deleteVia;
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getLong(Mockito.anyString());

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "compressed" -> unsoldItem.compressed;
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getBoolean(Mockito.anyString());
        return resultSet;
    }
}*/
