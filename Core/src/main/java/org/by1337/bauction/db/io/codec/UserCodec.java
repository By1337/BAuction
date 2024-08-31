package org.by1337.bauction.db.io.codec;

import org.by1337.bauction.db.kernel.User;
import org.by1337.blib.io.ByteBuffer;
import org.by1337.blib.nbt.NbtType;
import org.by1337.blib.nbt.impl.CompoundTag;

import java.util.UUID;

public class UserCodec implements Codec<User> {
    public static int CURRENT_VERSION = 201;
    public static final int MAGIC_NUMBER = 0xB13A0F3;

    @Override
    public User read(ByteBuffer buffer, int version) {
        if (version != CURRENT_VERSION) throw new IllegalArgumentException("Unknown version!");

        String nickName = buffer.readUtf();
        UUID uuid = buffer.readUUID();
        int dealCount = buffer.readVarInt();
        double dealSum = buffer.readDouble();
        CompoundTag extra = (CompoundTag) NbtType.COMPOUND.read(buffer);
        return new User(nickName, uuid, dealCount, dealSum, extra);
    }

    @Override
    public void write(User val, ByteBuffer buffer) {
        buffer.writeUtf(val.nickName);
        buffer.writeUUID(val.uuid);
        buffer.writeVarInt(val.dealCount);
        buffer.writeDouble(val.dealSum);
        val.extra.write(buffer);
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
