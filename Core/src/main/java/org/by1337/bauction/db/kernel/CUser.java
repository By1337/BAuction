package org.by1337.bauction.db.kernel;

import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.serialize.SerializeUtils;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CUser extends Placeholder implements User {
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
        init();
    }


    public CUser(@NotNull String nickName, @NotNull UUID uuid) {
        this.nickName = nickName;
        this.uuid = uuid;
        init();
    }
    private void init(){
        registerPlaceholder("{user_uuid}", () -> String.valueOf(uuid));
        registerPlaceholder("{deal_sum}", () -> String.valueOf(dealSum));
        registerPlaceholder("{nick_name}", () -> String.valueOf(nickName));
        registerPlaceholder("{deal_count}", () -> String.valueOf(dealCount));
        registerPlaceholder("{selling_item_count}", () -> String.valueOf(Main.getStorage().sellItemsCountByUser(uuid)));
        registerPlaceholder("{not_sold_item_count}", () -> String.valueOf(Main.getStorage().unsoldItemsCountByUser(uuid)));
        registerPlaceholder("{deal_count}", () -> String.valueOf(dealCount));
        registerPlaceholder("{external_slots}", () -> String.valueOf(externalSlots));
        registerPlaceholder("{slots_count}", () -> String.valueOf(Main.getCfg().getMaxSlots() + externalSlots));
        registerPlaceholder("{external_slots}", () -> String.valueOf(externalSlots));
        registerPlaceholder("{external_sell_time}", () -> String.valueOf(Main.getTimeUtil().getFormat(externalSellTime, false)));
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
            SerializeUtils.writeUUID(uuid, data);
            data.writeInt(dealCount);
            data.writeDouble(dealSum);
            data.flush();
            return out.toByteArray();
        }
    }

    public static CUser fromBytes(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {
            String nickName = in.readUTF();
            UUID uuid = SerializeUtils.readUUID(in);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CUser user)) return false;
        return Objects.equals(getNickName(), user.getNickName()) && Objects.equals(getUuid(), user.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNickName(), getUuid());
    }

}
