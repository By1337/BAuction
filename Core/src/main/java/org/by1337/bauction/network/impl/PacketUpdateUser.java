package org.by1337.bauction.network.impl;

import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.db.kernel.CUser;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class PacketUpdateUser extends Packet {
    private CUser user;

    public PacketUpdateUser(CUser user) {
        super(PacketType.UPDATE_USER);
        this.user = user;
    }

    public PacketUpdateUser() {
        super(PacketType.UPDATE_USER);
    }

    @Override
    public void write(DataOutputStream data) throws IOException {
        writeByteArray(data, user.getBytes());
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        user = CUser.fromBytes(readByteArray(in));
    }

    public CUser getUser() {
        return user;
    }
}
