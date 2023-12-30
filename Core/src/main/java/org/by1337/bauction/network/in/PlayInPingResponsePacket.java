package org.by1337.bauction.network.in;

import org.by1337.bauction.network.PacketIn;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.IOException;

public class PlayInPingResponsePacket extends PacketIn {

    private final int ping;
    private final String from;
    private final String to;

    public PlayInPingResponsePacket(DataInputStream in) throws IOException {
        super(PacketType.PING_RESPONSE);
        ping = in.readInt();
        from = in.readUTF();
        to = in.readUTF();
    }

    public int getPing() {
        return ping;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }
}
