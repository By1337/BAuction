package org.by1337.bauction.network.impl;

import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.db.kernel.CUser;
import org.by1337.bauction.network.ByteBuffer;
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
    public void write(ByteBuffer buffer) {
        try {
            byte[] arr = user.getBytes();
            buffer.writeVarInt(arr.length);
            buffer.writeBytes(arr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void read(ByteBuffer buffer) {
        try {
            byte[] arr = new byte[buffer.readVarInt()];
            buffer.readBytes(arr);
            user = CUser.fromBytes(arr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public CUser getUser() {
        return user;
    }
}
