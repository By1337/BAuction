package org.by1337.bauction.network.out;

import org.by1337.bauction.Main;
import org.by1337.bauction.network.PacketOut;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.serialize.SerializeUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayOutGiveMoneyRequest extends PacketOut {
    private final String from;
    private final String to;
    private final UUID player;
    private final Double count;
    private final int id;

    public PlayOutGiveMoneyRequest(String from, String to, UUID player, Double count, int id) {
        this.from = from;
        this.to = to;
        this.player = player;
        this.count = count;
        this.id = id;
    }

    public PlayOutGiveMoneyRequest(String to, UUID player, Double count, int id) {
        this(Main.getServerId(), to, player, count, id);
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeByte(PacketType.GIVE_MONEY_REQUEST.ordinal());
            data.writeUTF(from);
            data.writeUTF(to);
            SerializeUtils.writeUUID(player, data);
            data.writeDouble(count);
            data.writeInt(id);
            data.flush();
            return out.toByteArray();
        }
    }

    @Override
    public String toString() {
        return "PlayOutGiveMoneyRequest{" +
                "from='" + from + '\'' +
                ", to='" + to + '\'' +
                ", player=" + player +
                ", count=" + count +
                ", id=" + id +
                '}';
    }
}
