package org.by1337.bauction.network.out;

import org.by1337.bauction.network.PacketOut;
import org.by1337.bauction.network.PacketType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayOutPingRequestPacket extends PacketOut {
    private final long time;
    private final String server;

    public PlayOutPingRequestPacket(String server) {
        this.server = server;
        time = System.currentTimeMillis();
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeByte(PacketType.PING_REQUEST.ordinal());
            data.writeLong(time);
            data.writeUTF(server);
            data.flush();
            return out.toByteArray();
        }
    }

    public long getTime() {
        return time;
    }

    public String getServer() {
        return server;
    }
}