package org.by1337.bauction.network.in;

import org.by1337.bauction.db.kernel.CUser;
import org.by1337.bauction.network.PacketIn;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.IOException;

public class PlayInUpdateUserPacket extends PacketIn {
    private final CUser user;
    public PlayInUpdateUserPacket(DataInputStream in) throws IOException {
        super(PacketType.UPDATE_USER);
        user = CUser.fromBytes(readByteArray(in));
    }

    public CUser getUser() {
        return user;
    }
}
