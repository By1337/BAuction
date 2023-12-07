package org.by1337.bauction.network;

import org.by1337.bauction.serialize.SerializableToByteArray;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class Packet {

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
