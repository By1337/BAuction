package org.by1337.bauction.network.impl;

import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketPingResponse extends Packet {
    private int ping;
    private String from;
    private String to;

    public PacketPingResponse(int ping, String from, String to) {
        super(PacketType.PING_RESPONSE);
        this.ping = ping;
        this.from = from;
        this.to = to;
    }

    public PacketPingResponse() {
        super(PacketType.PING_RESPONSE);
    }

    @Override
    public void write(DataOutputStream data) throws IOException {
        data.writeInt(ping);
        data.writeUTF(from);
        data.writeUTF(to);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
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
