package org.by1337.bauction.network.impl;

import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    public void write(DataOutputStream data) throws IOException {
        writeByteArray(data, sellItem.getBytes());
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        sellItem = CSellItem.fromBytes(readByteArray(in));
    }

    public SellItem getSellItem() {
        return sellItem;
    }
}
