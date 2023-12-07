package org.by1337.bauction.network.out;

import org.by1337.bauction.network.PacketOut;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.util.UniqueName;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayOutRemoveUnsoldItemPacket extends PacketOut {
    private final UniqueName name;

    public PlayOutRemoveUnsoldItemPacket(UniqueName name) {
        this.name = name;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeByte(PacketType.REMOVE_UNSOLD_ITEM.ordinal());
            writeByteArray(data, name.getBytes());
            data.flush();
            return out.toByteArray();
        }
    }
}
