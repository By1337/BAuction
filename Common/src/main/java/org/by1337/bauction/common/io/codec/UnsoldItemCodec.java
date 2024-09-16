package org.by1337.bauction.common.io.codec;

import org.by1337.bauction.common.db.type.UnsoldItem;
import org.by1337.blib.nbt.NbtType;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.btcp.common.io.AbstractByteBuffer;

public class UnsoldItemCodec implements Codec<UnsoldItem> {
    public static int CURRENT_VERSION = 201;
    public static final int MAGIC_NUMBER = 0xB13A0F2;


    @Override
    public UnsoldItem read(AbstractByteBuffer buffer, int version) {
        if (version != CURRENT_VERSION) throw new IllegalArgumentException("Unknown version!");
        var builder = UnsoldItem.builder();
        NbtType nbtType = NbtType.values()[buffer.readByte()];
        builder.item(nbtType.read(buffer));
        builder.expired(buffer.readVarLong());
        builder.sellerUuid(buffer.readUUID());
        builder.id(buffer.readVarLong());
        builder.deleteVia(buffer.readVarLong());
        builder.extra((CompoundTag) NbtType.COMPOUND.read(buffer));
        return builder.build();
    }

    @Override
    public void write(UnsoldItem val, AbstractByteBuffer buffer) {
        buffer.writeByte(val.item.getType().ordinal());
        val.item.write(buffer);
        buffer.writeVarLong(val.expired);
        buffer.writeUUID(val.sellerUuid);
        buffer.writeVarLong(val.id);
        buffer.writeVarLong(val.deleteVia);
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
