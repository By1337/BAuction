package org.by1337.bauction.network.impl;

import org.by1337.bauction.db.kernel.User;
import org.by1337.bauction.network.ByteBuffer;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

import java.io.IOException;

public class PacketUpdateUser extends Packet {
    private User user;

    public PacketUpdateUser(User user) {
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
            user = User.fromBytes(arr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public User getUser() {
        return user;
    }
}
