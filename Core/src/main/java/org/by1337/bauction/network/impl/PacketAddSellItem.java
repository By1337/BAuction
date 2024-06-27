package org.by1337.bauction.network.impl;

import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.network.ByteBuffer;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

import java.io.IOException;

public class PacketAddSellItem extends Packet {
    private SellItem sellItem;

    public PacketAddSellItem(SellItem sellItem) {
        super(PacketType.ADD_SELL_ITEM);
        this.sellItem = sellItem;
    }

    public PacketAddSellItem() {
        super(PacketType.ADD_SELL_ITEM);
    }

    @Override
    public void write(ByteBuffer buffer) {
        try {
            byte[] arr = sellItem.getBytes();
            buffer.writeVarInt(arr.length);
            buffer.writeBytes(arr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void read(ByteBuffer buffer) {
        try {
            byte[] arr = new byte[buffer.readVarInt()];
            buffer.readBytes(arr);
            sellItem = SellItem.fromBytes(arr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public SellItem getSellItem() {
        return sellItem;
    }
}
