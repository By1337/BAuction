package org.by1337.bauction.network.in;

import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.network.PacketIn;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.IOException;

public class PlayInAddSellItemPacket extends PacketIn {
    private final SellItem sellItem;

    public PlayInAddSellItemPacket(DataInputStream in) throws IOException {
        super(PacketType.ADD_SELL_ITEM);
        sellItem = CSellItem.fromBytes(readByteArray(in));
    }

    public SellItem getSellItem() {
        return sellItem;
    }

}
