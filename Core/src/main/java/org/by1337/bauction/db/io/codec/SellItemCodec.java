package org.by1337.bauction.db.io.codec;

import org.bukkit.Material;
import org.by1337.bauction.api.util.UniqueName;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.util.id.CUniqueName;
import org.by1337.blib.io.ByteBuffer;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class SellItemCodec implements Codec<SellItem> {
    public static int CURRENT_VERSION = 200;
    @Override
    public SellItem read(ByteBuffer buffer, int version) {
        if (version != CURRENT_VERSION) throw new IllegalArgumentException("Unknown version!");
        String item = buffer.readUtf();
        String sellerName = buffer.readUtf();
        UUID sellerUuid = buffer.readUUID();
        double price = buffer.readDouble();
        boolean saleByThePiece = buffer.readBoolean();
        Set<String> tags = Set.copyOf(buffer.readStringList());
        long timeListedForSale = buffer.readVarLong();
        long removalDate = buffer.readVarLong();
        UniqueName uniqueName = new CUniqueName(buffer.readUtf());
        Material material = Material.values()[buffer.readVarInt()];
        int amount = buffer.readVarInt();
        double priceForOne = price / amount;
        Set<String> sellFor = Collections.emptySet();
        String server = buffer.readUtf();
        boolean compressed = buffer.readBoolean();
        return new SellItem(
                item,
                sellerName,
                sellerUuid,
                price,
                saleByThePiece,
                tags,
                timeListedForSale,
                removalDate,
                uniqueName,
                material,
                amount,
                priceForOne,
                sellFor,
                null,
                server,
                compressed
        );
    }

    @Override
    public void write(SellItem val, ByteBuffer buffer) {
        buffer.writeUtf(val.getItem());
        buffer.writeUtf(val.getSellerName());
        buffer.writeUUID(val.getSellerUuid());
        buffer.writeDouble(val.getPrice());
        buffer.writeBoolean(val.isSaleByThePiece());
        buffer.writeList(val.getTags(), ByteBuffer::writeUtf);
        buffer.writeVarLong(val.getTimeListedForSale());
        buffer.writeVarLong(val.getRemovalDate());
        buffer.writeUtf(val.getUniqueName().getKey());
        buffer.writeVarInt(val.getMaterial().ordinal());
        buffer.writeVarInt(val.getAmount());
        buffer.writeUtf(val.getServer());
        buffer.writeBoolean(val.isCompressed());
    }
}