package org.by1337.bauction.network.impl;

import org.by1337.bauction.network.ByteBuffer;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketPingRequest extends Packet {
    private long time;
    private String server;
    private String to;

    public PacketPingRequest(String server) {
        super(PacketType.PING_REQUEST);
        this.server = server;
        time = System.currentTimeMillis();
        to = "any";
    }

    public PacketPingRequest(String server, String to) {
        super(PacketType.PING_REQUEST);
        this.server = server;
        this.to = to;
        time = System.currentTimeMillis();
    }

    public PacketPingRequest() {
        super(PacketType.PING_REQUEST);
    }


    public long getTime() {
        return time;
    }

    public String getServer() {
        return server;
    }

    @Override
    public void write(ByteBuffer data) {
        data.writeVarLong(time);
        data.writeUtf(server);
        data.writeUtf(to);
    }

    @Override
    public void read(ByteBuffer in) {
        time = in.readVarLong();
        server = in.readUtf();
        to = in.readUtf();
    }

    public String getTo() {
        return to;
    }
}