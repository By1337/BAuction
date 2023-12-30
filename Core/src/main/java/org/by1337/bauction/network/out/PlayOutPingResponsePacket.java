package org.by1337.bauction.network.out;

import org.by1337.bauction.network.PacketOut;
import org.by1337.bauction.network.PacketType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayOutPingResponsePacket  extends PacketOut {
    private final int ping;
    private final String from;
    private final String to;

    public PlayOutPingResponsePacket(int ping, String from, String to) {
        this.ping = ping;
        this.from = from;
        this.to = to;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeByte(PacketType.PING_RESPONSE.ordinal());

            data.writeInt(ping);
            data.writeUTF(from);
            data.writeUTF(to);

            data.flush();
            return out.toByteArray();
        }
    }
}
