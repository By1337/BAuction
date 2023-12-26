package org.by1337.bauction.network.out;

import org.by1337.bauction.network.PacketOut;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.serialize.SerializeUtils;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayOutSendMessagePacket extends PacketOut {
    private final String message;
    private final UUID receiver;

    public PlayOutSendMessagePacket(String message, UUID receiver) {
        this.message = message;
        this.receiver = receiver;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeByte(PacketType.SEND_MESSAGE.ordinal());
            data.writeUTF(message);
            SerializeUtils.writeUUID(receiver, data);
            data.flush();
            return out.toByteArray();
        }
    }
}