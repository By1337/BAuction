package org.by1337.bauction.network.impl;

import org.by1337.bauction.api.auc.UnsoldItem;
import org.by1337.bauction.db.kernel.CUnsoldItem;
import org.by1337.bauction.network.ByteBuffer;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketAddUnsoldItem extends Packet {
    private CUnsoldItem unsoldItem;

    public PacketAddUnsoldItem(CUnsoldItem unsoldItem) {
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
            unsoldItem = CUnsoldItem.fromBytes(arr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CUnsoldItem getUnsoldItem() {
        return unsoldItem;
    }
}
