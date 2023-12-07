package org.by1337.bauction.network;

public interface PacketListener {
    void update(PacketIn packetIn);
    void connectionLost();
    void connectionRestored();
}
