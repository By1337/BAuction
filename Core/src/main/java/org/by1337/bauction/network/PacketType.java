package org.by1337.bauction.network;

import org.by1337.bauction.network.in.*;

import java.io.DataInputStream;
import java.io.IOException;

public enum PacketType {
    ADD_SELL_ITEM(PlayInAddSellItemPacket::new),
    REMOVE_SELL_ITEM(PlayInRemoveSellItemPacket::new),
    ADD_UNSOLD_ITEM(PlayInAddUnsoldItemPacket::new),
    REMOVE_UNSOLD_ITEM(PlayInRemoveUnsoldItemPacket::new),
    UPDATE_USER(PlayInUpdateUserPacket::new),
    SEND_MESSAGE(PlayInSendMessagePacket::new);

    private final PacketInSuppler<? extends PacketIn> suppler;

    PacketType(PacketInSuppler<? extends PacketIn> suppler) {
        this.suppler = suppler;
    }

    public PacketInSuppler<? extends PacketIn> getSuppler() {
        return suppler;
    }

    public static interface PacketInSuppler<T extends PacketIn> {
        T get(DataInputStream in) throws IOException;
    }
}
