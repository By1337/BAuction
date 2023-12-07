package org.by1337.bauction.network.out;

import org.by1337.bauction.auc.UnsoldItem;
import org.by1337.bauction.network.PacketOut;
import org.by1337.bauction.network.PacketType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayOutAddUnsoldItemPacket extends PacketOut {
    private final UnsoldItem unsoldItem;

    public PlayOutAddUnsoldItemPacket(UnsoldItem unsoldItem) {
        this.unsoldItem = unsoldItem;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeByte(PacketType.ADD_UNSOLD_ITEM.ordinal());
            writeByteArray(data, unsoldItem.getBytes());
            data.flush();
            return out.toByteArray();
        }
    }
}
