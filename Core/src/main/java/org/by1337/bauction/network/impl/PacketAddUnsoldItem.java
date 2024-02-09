package org.by1337.bauction.network.impl;

import org.by1337.bauction.api.auc.UnsoldItem;
import org.by1337.bauction.db.kernel.CUnsoldItem;
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
    public void write(DataOutputStream data) throws IOException {
        writeByteArray(data, unsoldItem.getBytes());
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        unsoldItem = CUnsoldItem.fromBytes(readByteArray(in));
    }

    public CUnsoldItem getUnsoldItem() {
        return unsoldItem;
    }
}
