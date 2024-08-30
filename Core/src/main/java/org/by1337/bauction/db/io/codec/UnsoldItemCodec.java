package org.by1337.bauction.db.io.codec;

import org.by1337.bauction.db.kernel.UnsoldItem;
import org.by1337.blib.io.ByteBuffer;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.NbtType;

import java.util.UUID;

public class UnsoldItemCodec implements Codec<UnsoldItem> {
    public static int CURRENT_VERSION = 200;
    public static final int MAGIC_NUMBER = 0xB13A0F2;

    @Override
    public UnsoldItem read(ByteBuffer buffer, int version) {
        if (version != CURRENT_VERSION) throw new IllegalArgumentException("Unknown version!");

        NbtType nbtType = NbtType.values()[buffer.readByte()];
        NBT item = nbtType.read(buffer);
        long expired = buffer.readVarLong();
        UUID sellerUuid = buffer.readUUID();
        long id = buffer.readVarLong();
        long deleteVia = buffer.readVarLong();
        return new UnsoldItem(item, expired, sellerUuid, id, deleteVia);
    }

    @Override
    public void write(UnsoldItem val, ByteBuffer buffer) {
        buffer.writeByte(val.item.getType().ordinal());
        val.item.write(buffer);
        buffer.writeVarLong(val.expired);
        buffer.writeUUID(val.sellerUuid);
        buffer.writeVarLong(val.id);
        buffer.writeVarLong(val.deleteVia);
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
