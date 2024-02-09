package org.by1337.bauction.network.impl;

import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.serialize.SerializeUtils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

public class PacketSendMessage extends Packet {
    private String message;
    private UUID receiver;

    public PacketSendMessage(String message, UUID receiver) {
        super(PacketType.SEND_MESSAGE);
        this.message = message;
        this.receiver = receiver;
    }

    public PacketSendMessage() {
        super(PacketType.SEND_MESSAGE);
    }


    @Override
    public void write(DataOutputStream data) throws IOException {
        data.writeUTF(message);
        SerializeUtils.writeUUID(receiver, data);
    }

    @Override
    public void read(DataInputStream in) throws IOException {
        message = in.readUTF();
        receiver = SerializeUtils.readUUID(in);
    }

    public String getMessage() {
        return message;
    }

    public UUID getReceiver() {
        return receiver;
    }
}