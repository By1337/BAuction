package org.by1337.bauction.network;

import java.io.IOException;

public abstract class PacketIn extends Packet {
    private final PacketType<?> type;

    public PacketIn(PacketType<?> type) {
        this.type = type;
    }

    public PacketType<?> getType() {
        return type;
    }

    @Override
    public String toString() {
        return "PacketIn{" +
                "type=" + type +
                '}';
    }
}
