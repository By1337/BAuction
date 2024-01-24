package org.by1337.bauction.network.out;

import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.network.PacketOut;
import org.by1337.bauction.network.PacketType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PlayOutUpdateUserPacket extends PacketOut {
    private final User user;

    public PlayOutUpdateUserPacket(User user) {
        this.user = user;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeByte(PacketType.UPDATE_USER.ordinal());
            writeByteArray(data, user.getBytes());
            data.flush();
            return out.toByteArray();
        }
    }
}
