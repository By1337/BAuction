package org.by1337.bauction.network.impl;

import org.by1337.bauction.Main;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.serialize.SerializeUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PacketGiveMoneyRequest extends Packet {
    private String from;
    private String to;
    private UUID player;
    private Double count;
    private int id;

    public PacketGiveMoneyRequest(String from, String to, UUID player, Double count, int id) {
        super(PacketType.GIVE_MONEY_REQUEST);
        this.from = from;
        this.to = to;
        this.player = player;
        this.count = count;
        this.id = id;
    }

    public PacketGiveMoneyRequest(String to, UUID player, Double count, int id) {
        this(Main.getServerId(), to, player, count, id);
    }

    public PacketGiveMoneyRequest() {
        super(PacketType.GIVE_MONEY_REQUEST);
    }


    @Override
    public String toString() {
        return "PacketGiveMoneyRequest{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", player=" + player +
                ", count=" + count +
                ", id=" + id +
                '}';
    }

    @Override
    public void write(DataOutputStream data) throws IOException {
        data.writeUTF(from);
        data.writeUTF(to);
        SerializeUtils.writeUUID(player, data);
        data.writeDouble(count);
        data.writeInt(id);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
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
