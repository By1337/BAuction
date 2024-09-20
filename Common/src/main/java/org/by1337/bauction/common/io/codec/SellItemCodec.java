package org.by1337.bauction.common.io.codec;

import org.by1337.bauction.common.db.type.SellItem;
import org.by1337.blib.nbt.NbtType;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.btcp.common.io.AbstractByteBuffer;

import java.util.Set;

public class SellItemCodec implements Codec<SellItem> {
    public static int CURRENT_VERSION = 202;
    public static final int MAGIC_NUMBER = 0xB13A0F1;

    @Override
    public SellItem read(AbstractByteBuffer buffer, int version) {
        if (version != CURRENT_VERSION) throw new IllegalArgumentException("Unknown version!");
        var builder = SellItem.builder();
        NbtType nbtType = NbtType.values()[buffer.readByte()];
        builder.item(nbtType.read(buffer));
        builder.sellerName(buffer.readUtf());
        builder.sellerUuid(buffer.readUUID());
        builder.price(buffer.readDouble());
        builder.saleByThePiece(buffer.readBoolean());
        builder.tags(Set.copyOf(buffer.readStringList()));
        builder.timeListedForSale(buffer.readVarLong());
        builder.removalDate(buffer.readVarLong());
        builder.id(buffer.readVarLong());
        builder.material(buffer.readVarInt());
        builder.amount(buffer.readVarInt());
        builder.server(buffer.readUUID());
        builder.extra((CompoundTag) NbtType.COMPOUND.read(buffer));
        return builder.build();
    }

    @Override
    public void write(SellItem val, AbstractByteBuffer buffer) {
        buffer.writeByte(val.item.getType().ordinal());
        val.item.write(buffer);
        buffer.writeUtf(val.sellerName);
        buffer.writeUUID(val.sellerUuid);
        buffer.writeDouble(val.price);
        buffer.writeBoolean(val.saleByThePiece);
        buffer.writeList(val.tags, AbstractByteBuffer::writeUtf);
        buffer.writeVarLong(val.timeListedForSale);
        buffer.writeVarLong(val.removalDate);
        buffer.writeVarLong(val.id);
        buffer.writeVarInt(val.material);
        buffer.writeVarInt(val.amount);
        buffer.writeUUID(val.server);
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
