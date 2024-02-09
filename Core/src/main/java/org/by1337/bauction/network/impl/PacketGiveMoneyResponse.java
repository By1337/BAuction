package org.by1337.bauction.network.impl;

import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketGiveMoneyResponse extends Packet {
    private String to;
    private int id;

    public PacketGiveMoneyResponse(String to, int id) {
        super(PacketType.GIVE_MONEY_RESPONSE);
        this.to = to;
        this.id = id;
    }

    public PacketGiveMoneyResponse() {
        super(PacketType.GIVE_MONEY_RESPONSE);
    }


    @Override
    public void write(DataOutputStream data) throws IOException {
        data.writeUTF(to);
        data.writeInt(id);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        to = in.readUTF();
        id = in.readInt();
    }

    public String getTo() {
        return to;
    }

    public int getId() {
        return id;
    }
}
