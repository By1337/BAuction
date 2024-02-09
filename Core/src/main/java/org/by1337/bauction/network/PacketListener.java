package org.by1337.bauction.network;

public interface PacketListener {
    void update(Packet packetIn);
    void connectionLost();
    void connectionRestored();
}
