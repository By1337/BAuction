package org.by1337.bauction.network.in;

import org.by1337.bauction.network.PacketIn;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.IOException;

public class PlayInPingRequestPacket extends PacketIn {

    private final long time;
    private final String server;

    public PlayInPingRequestPacket(DataInputStream in) throws IOException {
        super(PacketType.PING_REQUEST);
        time = in.readLong();
        server = in.readUTF();
    }

    public long getTime() {
        return time;
    }

    public String getServer() {
        return server;
    }
}
