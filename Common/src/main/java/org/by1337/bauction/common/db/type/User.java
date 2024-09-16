package org.by1337.bauction.common.db.type;

import org.by1337.bauction.common.io.codec.Codec;
import org.by1337.bauction.common.io.codec.UserCodec;
import org.by1337.blib.nbt.impl.CompoundTag;

import java.util.Objects;
import java.util.UUID;

public class User {
    public static final Codec<User> CODEC = new UserCodec();
    public final String nickName;
    public final UUID uuid;
    public final int dealCount;
    public final double dealSum;
    public final int externalSlots;
    public final long externalSellTime;
    public final CompoundTag extra;

    public User(String nickName, UUID uuid, int dealCount, double dealSum, int externalSlots, long externalSellTime, CompoundTag extra) {
        this.nickName = nickName;
        this.uuid = uuid;
        this.dealCount = dealCount;
        this.dealSum = dealSum;
        this.externalSlots = externalSlots;
        this.externalSellTime = externalSellTime;
        this.extra = extra;
    }

    private User(Builder builder) {
        this.nickName = builder.nickName;
        this.uuid = builder.uuid;
        this.dealCount = builder.dealCount;
        this.dealSum = builder.dealSum;
        this.externalSlots = builder.externalSlots;
        this.externalSellTime = builder.externalSellTime;
        this.extra = builder.extra;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(User user) {
        return new Builder(user);
    }

    public User setNickName(String nickName) {
        return User.builder(this).nickName(nickName).build();
    }

    public User setUuid(UUID uuid) {
        return User.builder(this).uuid(uuid).build();
    }

    public User setDealCount(int dealCount) {
        return User.builder(this).dealCount(dealCount).build();
    }

    public User setDealSum(double dealSum) {
        return User.builder(this).dealSum(dealSum).build();
    }

    public User setExternalSlots(int externalSlots) {
        return User.builder(this).externalSlots(externalSlots).build();
    }

    public User setExternalSellTime(long externalSellTime) {
        return User.builder(this).externalSellTime(externalSellTime).build();
    }

    public User setExtra(CompoundTag extra) {
        return User.builder(this).extra(extra).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return dealCount == user.dealCount && Double.compare(dealSum, user.dealSum) == 0 && externalSlots == user.externalSlots && externalSellTime == user.externalSellTime && Objects.equals(nickName, user.nickName) && Objects.equals(uuid, user.uuid) && Objects.equals(extra, user.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nickName, uuid, dealCount, dealSum, externalSlots, externalSellTime, extra);
    }

    public static class Builder {

        private String nickName;
        private UUID uuid;
        private int dealCount = 0;
        private double dealSum = 0;
        private int externalSlots = 0;
        private long externalSellTime = 0;
        private CompoundTag extra;

        public Builder() {
        }

        public Builder(User user) {
            this.nickName = user.nickName;
            this.uuid = user.uuid;
            this.dealCount = user.dealCount;
            this.dealSum = user.dealSum;
            this.externalSlots = user.externalSlots;
            this.externalSellTime = user.externalSellTime;
            this.extra = user.extra;
        }

        Builder(String nickName, UUID uuid, int dealCount, double dealSum, int externalSlots, long externalSellTime, CompoundTag extra) {
            this.nickName = nickName;
            this.uuid = uuid;
            this.dealCount = dealCount;
            this.dealSum = dealSum;
            this.externalSlots = externalSlots;
            this.externalSellTime = externalSellTime;
            this.extra = extra;
        }

        public Builder nickName(String nickName) {
            this.nickName = nickName;
            return Builder.this;
        }

        public Builder uuid(UUID uuid) {
            this.uuid = uuid;
            return Builder.this;
        }

        public Builder dealCount(int dealCount) {
            this.dealCount = dealCount;
            return Builder.this;
        }

        public Builder dealSum(double dealSum) {
            this.dealSum = dealSum;
            return Builder.this;
        }

        public Builder externalSlots(int externalSlots) {
            this.externalSlots = externalSlots;
            return Builder.this;
        }

        public Builder externalSellTime(long externalSellTime) {
            this.externalSellTime = externalSellTime;
            return Builder.this;
        }

        public Builder extra(CompoundTag extra) {
            this.extra = extra;
            return Builder.this;
        }

        public User build() {
            if (this.nickName == null) {
                throw new NullPointerException("The property \"nickName\" is null. "
                                               + "Please set the value by \"nickName()\". "
                                               + "The properties \"nickName\", \"uuid\" and \"extra\" are required.");
            }
            if (this.uuid == null) {
                throw new NullPointerException("The property \"uuid\" is null. "
                                               + "Please set the value by \"uuid()\". "
                                               + "The properties \"nickName\", \"uuid\" and \"extra\" are required.");
            }
            if (this.extra == null) {
                throw new NullPointerException("The property \"extra\" is null. "
                                               + "Please set the value by \"extra()\". "
                                               + "The properties \"nickName\", \"uuid\" and \"extra\" are required.");
            }

            return new User(this);
        }
    }
}
