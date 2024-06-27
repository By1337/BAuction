package org.by1337.bauction.network.impl;

import org.by1337.bauction.db.kernel.UnsoldItem;
import org.by1337.bauction.network.ByteBuffer;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

import java.io.IOException;

public class PacketAddUnsoldItem extends Packet {
    private UnsoldItem unsoldItem;

    public PacketAddUnsoldItem(UnsoldItem unsoldItem) {
        super(PacketType.ADD_UNSOLD_ITEM);
        this.unsoldItem = unsoldItem;
    }

    public PacketAddUnsoldItem() {
        super(PacketType.ADD_UNSOLD_ITEM);
    }


    @Override
    public void write(ByteBuffer buffer) {
        try {
            byte[] arr = unsoldItem.getBytes();
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
            unsoldItem = UnsoldItem.fromBytes(arr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public UnsoldItem getUnsoldItem() {
        return unsoldItem;
    }
}
