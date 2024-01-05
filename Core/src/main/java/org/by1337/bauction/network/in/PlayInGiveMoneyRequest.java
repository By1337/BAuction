package org.by1337.bauction.network.in;

import org.by1337.bauction.network.PacketIn;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.serialize.SerializeUtils;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayInGiveMoneyRequest extends PacketIn {
    private final String from;
    private final String to;
    private final UUID player;
    private final Double count;
    private final int id;
    public PlayInGiveMoneyRequest(DataInputStream in) throws IOException {
        super(PacketType.GIVE_MONEY_REQUEST);
        from = in.readUTF();
        to = in.readUTF();
        player = SerializeUtils.readUUID(in);
        count = in.readDouble();
        id = in.readInt();
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public UUID getPlayer() {
        return player;
    }

    public Double getCount() {
        return count;
    }

    public int getId() {
        return id;
    }
}
