package org.by1337.bauction.network.out;

import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketOut;
import org.by1337.bauction.network.PacketType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayOutAddSellItemPacket extends PacketOut {
    private final SellItem sellItem;

    public PlayOutAddSellItemPacket(SellItem sellItem) {
        this.sellItem = sellItem;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeByte(PacketType.ADD_SELL_ITEM.ordinal());
            writeByteArray(data, sellItem.getBytes());
            data.flush();
            return out.toByteArray();
        }
    }
}
