package org.by1337.bauction.network.in;

import org.by1337.bauction.network.PacketIn;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.IOException;

public class PlayInGiveMoneyResponse extends PacketIn {

    private final String to;
    private final int id;
    public PlayInGiveMoneyResponse(DataInputStream in) throws IOException {
        super(PacketType.GIVE_MONEY_RESPONSE);
        to = in.readUTF();
        id = in.readInt();
    }

    public String getTo() {
        return to;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return "PlayInGiveMoneyResponse{" +
                "to='" + to + '\'' +
                ", id=" + id +
                '}';
    }
}
