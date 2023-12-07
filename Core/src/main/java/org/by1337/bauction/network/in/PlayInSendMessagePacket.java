package org.by1337.bauction.network.in;

import org.by1337.bauction.db.kernel.CUser;
import org.by1337.bauction.network.PacketIn;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.UUID;

public class PlayInSendMessagePacket extends PacketIn {

    private final String message;
    private final UUID receiver;

    public PlayInSendMessagePacket(DataInputStream in) throws IOException {
        super(PacketType.SEND_MESSAGE);
        message = in.readUTF();
        receiver = UUID.fromString(in.readUTF());
    }

    public String getMessage() {
        return message;
    }

    public UUID getReceiver() {
        return receiver;
    }
}
