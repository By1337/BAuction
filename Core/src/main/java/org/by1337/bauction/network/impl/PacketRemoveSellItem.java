package org.by1337.bauction.network.impl;

import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.api.util.UniqueName;
import org.by1337.bauction.util.CUniqueName;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketRemoveSellItem extends Packet {
    private UniqueName name;

    public PacketRemoveSellItem(UniqueName name) {
        super(PacketType.REMOVE_SELL_ITEM);
        this.name = name;
    }

    public PacketRemoveSellItem() {
        super(PacketType.REMOVE_SELL_ITEM);
    }

    @Override
    public void write(DataOutputStream data) throws IOException {
        writeByteArray(data, name.getBytes());
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        name = CUniqueName.fromBytes(readByteArray(in));
    }

    public UniqueName getName() {
        return name;
    }
}
