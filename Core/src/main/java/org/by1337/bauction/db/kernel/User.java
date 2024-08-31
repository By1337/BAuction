package org.by1337.bauction.db.kernel;

import org.by1337.bauction.Main;
import org.by1337.bauction.api.serialize.SerializableToByteArray;
import org.by1337.bauction.db.io.codec.Codec;
import org.by1337.bauction.db.io.codec.UserCodec;
import org.by1337.bauction.serialize.SerializeUtils;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.*;

public class User extends Placeholder implements SerializableToByteArray {
    public static final Codec<User> CODEC = new UserCodec();
    public final String nickName;
    public final UUID uuid;
    public int dealCount;
    public double dealSum;
    private transient int externalSlots = 0;
    private transient long externalSellTime = 0L;
    public final CompoundTag extra;


    public User(String nickName, UUID uuid, int dealCount, double dealSum, CompoundTag extra) {
        this.nickName = nickName;
        this.uuid = uuid;
        this.dealCount = dealCount;
        this.dealSum = dealSum;
        this.extra = extra;
        init();
    }

    public int getMaxItems() {
        return Integer.MAX_VALUE; // todo limits
    }

    public User(@NotNull String nickName, @NotNull UUID uuid, CompoundTag extra) {
        this.nickName = nickName;
        this.uuid = uuid;
        this.extra = extra;
        init();
    }

    private void init() {
        registerPlaceholder("{user_uuid}", () -> String.valueOf(uuid));
        registerPlaceholder("{deal_sum}", () -> String.valueOf(dealSum));
        registerPlaceholder("{nick_name}", () -> String.valueOf(nickName));
        registerPlaceholder("{deal_count}", () -> String.valueOf(dealCount));
        registerPlaceholder("{selling_item_count}", () -> String.valueOf(Main.getStorage().getSellItemsCountByUser(uuid)));
        registerPlaceholder("{not_sold_item_count}", () -> String.valueOf(Main.getStorage().getUnsoldItemsCountByUser(uuid)));
        registerPlaceholder("{external_slots}", () -> String.valueOf(externalSlots));
        registerPlaceholder("{slots_count}", () -> String.valueOf(Main.getCfg().getMaxSlots() + externalSlots));
        registerPlaceholder("{external_sell_time}", () -> String.valueOf(Main.getTimeUtil().getFormat(externalSellTime, false)));
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
        return "User{" +
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
        if (!(o instanceof User user)) return false;
        return Objects.equals(getNickName(), user.getNickName()) && Objects.equals(getUuid(), user.getUuid());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNickName(), getUuid());
    }

}
