package org.by1337.bauction.network.out;

import org.by1337.bauction.network.PacketOut;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.serialize.SerializeUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayOutGiveMoneyResponse extends PacketOut {
    private final String to;
    private final int id;

    public PlayOutGiveMoneyResponse(String to, int id) {
        this.to = to;
        this.id = id;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeByte(PacketType.GIVE_MONEY_RESPONSE.ordinal());
            data.writeUTF(to);
            data.writeInt(id);
            data.flush();
            return out.toByteArray();
        }
    }
}
