package org.by1337.bauction.common.io.codec;

import org.by1337.bauction.common.db.type.User;
import org.by1337.blib.nbt.NbtType;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.btcp.common.io.AbstractByteBuffer;

public class UserCodec implements Codec<User> {
    public static int CURRENT_VERSION = 201;
    public static final int MAGIC_NUMBER = 0xB13A0F3;

    @Override
    public User read(AbstractByteBuffer buffer, int version) {
        if (version != CURRENT_VERSION) throw new IllegalArgumentException("Unknown version!");
        var builder = User.builder();
        builder.nickName(buffer.readUtf());
        builder.uuid(buffer.readUUID());
        builder.dealCount(buffer.readVarInt());
        builder.dealSum(buffer.readDouble());
        builder.extra((CompoundTag) NbtType.COMPOUND.read(buffer));
        builder.externalSlots(buffer.readVarInt());
        builder.externalSellTime(buffer.readVarLong());
        return builder.build();
    }

    @Override
    public void write(User val, AbstractByteBuffer buffer) {
        buffer.writeUtf(val.nickName);
        buffer.writeUUID(val.uuid);
        buffer.writeVarInt(val.dealCount);
        buffer.writeDouble(val.dealSum);
        val.extra.write(buffer);
        buffer.writeVarInt(val.externalSlots);
        buffer.writeVarLong(val.externalSellTime);
    }

    @Override
    public int getVersion() {
        return CURRENT_VERSION;
    }

    @Override
    public int getMagicNumber() {
        return MAGIC_NUMBER;
    }
}
