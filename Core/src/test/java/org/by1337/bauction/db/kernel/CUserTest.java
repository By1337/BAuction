package org.by1337.bauction.db.kernel;

import junit.framework.TestCase;
import org.junit.Assert;
import org.mockito.Mockito;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class CUserTest extends TestCase {
    private CUser user = new CUser(
            "_By1337_",
            UUID.nameUUIDFromBytes("OfflinePlayer:_By1337_".getBytes()),
            0,
            10
    );

    public void testFromResultSet() throws SQLException {
        Assert.assertEquals(CUser.fromResultSet(createResultSet()), user);
    }

    public void testGetBytes() throws IOException {
        Assert.assertEquals(CUser.fromBytes(user.getBytes()), user);
    }

    private ResultSet createResultSet() throws SQLException {
        ResultSet resultSet = Mockito.mock(ResultSet.class);

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "name" -> user.nickName;
                case "uuid" -> user.uuid.toString();
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getString(Mockito.anyString());

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "deal_count" -> user.dealCount;
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getInt(Mockito.anyString());

        Mockito.doAnswer(invocation -> {
            String str = invocation.getArgument(0);
            return switch (str) {
                case "deal_sum" -> user.dealSum;
                default -> throw new IllegalArgumentException();
            };
        }).when(resultSet).getDouble(Mockito.anyString());

        return resultSet;
    }
}