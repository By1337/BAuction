package org.by1337.bauction.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {

    private final PacketType<?> type;

    public Packet(PacketType<?> type) {
        this.type = type;
    }

    public PacketType<?> getType() {
        return type;
    }

    public abstract void write(DataOutputStream data) throws IOException;
    public abstract void read(DataInputStream in) throws IOException;

    protected void writeByteArray(DataOutputStream data, byte[] source) throws IOException {
        data.writeInt(source.length);
        for (byte b : source) {
            data.writeByte(b);
        }
    }

    protected byte[] readByteArray(DataInputStream in) throws IOException {
        int len = in.readInt();
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = in.readByte();
        }
        return result;
    }
}
