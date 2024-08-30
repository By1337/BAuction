package org.by1337.bauction.db.io.codec;

import org.bukkit.Material;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.blib.io.ByteBuffer;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.NbtType;

import java.util.Set;
import java.util.UUID;

public class SellItemCodec implements Codec<SellItem> {
    public static int CURRENT_VERSION = 200;
    public static final int MAGIC_NUMBER = 0xB13A0F1;

    @Override
    public SellItem read(ByteBuffer buffer, int version) {
        if (version != CURRENT_VERSION) throw new IllegalArgumentException("Unknown version!");
        NbtType nbtType = NbtType.values()[buffer.readByte()];
        NBT item = nbtType.read(buffer);
        String sellerName = buffer.readUtf();
        UUID sellerUuid = buffer.readUUID();
        double price = buffer.readDouble();
        boolean saleByThePiece = buffer.readBoolean();
        Set<String> tags = Set.copyOf(buffer.readStringList());
        long timeListedForSale = buffer.readVarLong();
        long removalDate = buffer.readVarLong();
        long id = buffer.readVarLong();
        Material material = Material.values()[buffer.readVarInt()];
        int amount = buffer.readVarInt();
        double priceForOne = price / amount;
        String server = buffer.readUtf();
        return new SellItem(
                item,
                sellerName,
                sellerUuid,
                price,
                saleByThePiece,
                tags,
                timeListedForSale,
                removalDate,
                id,
                material,
                amount,
                priceForOne,
                null,
                server
        );
    }

    @Override
    public void write(SellItem val, ByteBuffer buffer) {
        buffer.writeByte(val.getItem().getType().ordinal());
        val.getItem().write(buffer);
        buffer.writeUtf(val.getSellerName());
        buffer.writeUUID(val.getSellerUuid());
        buffer.writeDouble(val.getPrice());
        buffer.writeBoolean(val.isSaleByThePiece());
        buffer.writeList(val.getTags(), ByteBuffer::writeUtf);
        buffer.writeVarLong(val.getTimeListedForSale());
        buffer.writeVarLong(val.getRemovalDate());
        buffer.writeVarLong(val.getId());
        buffer.writeVarInt(val.getMaterial().ordinal());
        buffer.writeVarInt(val.getAmount());
        buffer.writeUtf(val.getServer());
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
