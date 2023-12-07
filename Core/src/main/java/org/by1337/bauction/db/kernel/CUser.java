package org.by1337.bauction.db.kernel;

import org.by1337.bauction.Main;
import org.by1337.bauction.auc.User;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.util.UniqueName;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CUser implements User {
    final String nickName;
    final UUID uuid;
    int dealCount;
    double dealSum;
    private transient int externalSlots = 0;
    private transient long externalSellTime = 0L;
    private transient int lastHash;

    public boolean hasChanges() {
        return Objects.hash(dealCount, dealSum) != lastHash;
    }

    public void updateHash() {
        lastHash = Objects.hash(dealCount, dealSum);
    }


    public CUser(String nickName, UUID uuid, int dealCount, double dealSum) {
        this.nickName = nickName;
        this.uuid = uuid;
        this.dealCount = dealCount;
        this.dealSum = dealSum;
    }


    public CUser(@NotNull String nickName, @NotNull UUID uuid) {
        this.nickName = nickName;
        this.uuid = uuid;
    }

    public String toSql(String table) {
        return String.format(
                "INSERT INTO %s (uuid, name, deal_count, deal_sum) VALUES ('%s', '%s', %s, %s)", table,
                uuid, nickName, dealCount, dealSum
        );
    }

    public String toSqlUpdate(String table) {
        return String.format(
                "UPDATE %s SET name = '%s', deal_count = %s, deal_sum = %s WHERE uuid = '%s'", table,
                nickName, dealCount, dealSum, uuid
        );
    }

    public static CUser fromResultSet(ResultSet resultSet) throws SQLException {
        String nickName = resultSet.getString("name");
        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        int dealCount = resultSet.getInt("deal_count");
        double dealSum = resultSet.getDouble("deal_sum");

        return new CUser(nickName, uuid, dealCount, dealSum);
    }

    public boolean isValid() {
        return nickName != null && uuid != null;
    }

    public void setExternalSlots(int externalSlots) {
        this.externalSlots = externalSlots;
    }

    public void setExternalSellTime(long externalSellTime) {
        this.externalSellTime = externalSellTime;
    }

    @Override
    public String toString() {
        return "CUser{" +
                "nickName='" + nickName + '\'' +
                ", uuid=" + uuid +
                ", externalSlots=" + externalSlots +
                ", externalSellTime=" + externalSellTime +
                ", dealCount=" + dealCount +
                ", dealSum=" + dealSum +
                '}';
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeUTF(nickName);
            data.writeUTF(uuid.toString());
            data.writeInt(dealCount);
            data.writeDouble(dealSum);
            data.flush();
            return out.toByteArray();
        }
    }

    public static CUser fromBytes(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {
            String nickName = in.readUTF();
            UUID uuid = UUID.fromString(in.readUTF());
            int dealCount = in.readInt();
            double dealSum = in.readDouble();

            return new CUser(
                    nickName, uuid, dealCount, dealSum
            );
        }
    }

    public String getNickName() {
        return nickName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getDealCount() {
        return dealCount;
    }

    public double getDealSum() {
        return dealSum;
    }

    public int getExternalSlots() {
        return externalSlots;
    }

    public long getExternalSellTime() {
        return externalSellTime;
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (true) {
            if (sb.indexOf("{deal_sum}") != -1) {
                sb.replace(sb.indexOf("{deal_sum}"), sb.indexOf("{deal_sum}") + "{deal_sum}".length(), String.valueOf(dealSum));
                continue;
            }
            if (sb.indexOf("{nick_name}") != -1) {
                sb.replace(sb.indexOf("{nick_name}"), sb.indexOf("{nick_name}") + "{nick_name}".length(), nickName);
                continue;
            }
            if (sb.indexOf("{deal_count}") != -1) {
                sb.replace(sb.indexOf("{deal_count}"), sb.indexOf("{deal_count}") + "{deal_count}".length(), String.valueOf(dealCount));
                continue;
            }
            if (sb.indexOf("{selling_item_count}") != -1) {
                sb.replace(sb.indexOf("{selling_item_count}"), sb.indexOf("{selling_item_count}") + "{selling_item_count}".length(),
                        String.valueOf(Main.getStorage().getSellItemsByUser(uuid).size())
                );
                continue;
            }
            if (sb.indexOf("{not_sold_item_count}") != -1) {
                sb.replace(sb.indexOf("{not_sold_item_count}"), sb.indexOf("{not_sold_item_count}") + "{not_sold_item_count}".length(),
                        String.valueOf(Main.getStorage().getUnsoldItemsByUser(uuid).size())
                );
                continue;
            }
            if (sb.indexOf("{external_slots}") != -1) {
                sb.replace(sb.indexOf("{external_slots}"), sb.indexOf("{external_slots}") + "{external_slots}".length(), String.valueOf(externalSlots));
                continue;
            }
            if (sb.indexOf("{slots_count}") != -1) {
                sb.replace(sb.indexOf("{slots_count}"), sb.indexOf("{slots_count}") + "{slots_count}".length(), String.valueOf(Main.getCfg().getMaxSlots() + externalSlots));
                continue;
            }
            if (sb.indexOf("{external_sell_time}") != -1) {
                sb.replace(sb.indexOf("{external_sell_time}"), sb.indexOf("{external_sell_time}") + "{external_sell_time}".length(), Main.getTimeUtil().getFormat(externalSellTime, false));
                continue;
            }
            break;
        }
        return sb.toString();
    }
}
