package org.by1337.bauction.network.in;

import org.by1337.bauction.auc.UnsoldItem;
import org.by1337.bauction.db.kernel.CUnsoldItem;
import org.by1337.bauction.network.PacketIn;
import org.by1337.bauction.network.PacketType;

import java.io.DataInputStream;
import java.io.IOException;

public class PlayInAddUnsoldItemPacket extends PacketIn {
    private final CUnsoldItem unsoldItem;

    public PlayInAddUnsoldItemPacket(DataInputStream in) throws IOException {
        super(PacketType.ADD_UNSOLD_ITEM);
        unsoldItem = CUnsoldItem.fromBytes(readByteArray(in));
    }

    public CUnsoldItem getUnsoldItem() {
        return unsoldItem;
    }
}
