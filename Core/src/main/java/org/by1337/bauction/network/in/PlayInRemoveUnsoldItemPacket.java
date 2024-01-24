package org.by1337.bauction.network.in;

import org.by1337.bauction.network.PacketIn;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.api.util.UniqueName;

import java.io.DataInputStream;
import java.io.IOException;

public class PlayInRemoveUnsoldItemPacket extends PacketIn {
    private final UniqueName name;

    public PlayInRemoveUnsoldItemPacket(DataInputStream in) throws IOException {
        super(PacketType.REMOVE_UNSOLD_ITEM);
        name = CUniqueName.fromBytes(readByteArray(in));
    }

    public UniqueName getName() {
        return name;
    }
}
