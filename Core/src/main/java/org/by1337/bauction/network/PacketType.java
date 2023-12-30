package org.by1337.bauction.network;

import org.by1337.bauction.network.in.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class PacketType<T extends PacketIn> {
    private static final List<PacketType<?>> types = new ArrayList<>();

    public static final PacketType<PlayInAddSellItemPacket> ADD_SELL_ITEM = register(PlayInAddSellItemPacket::new);
    public static final PacketType<PlayInRemoveSellItemPacket> REMOVE_SELL_ITEM = register(PlayInRemoveSellItemPacket::new);
    public static final PacketType<PlayInAddUnsoldItemPacket> ADD_UNSOLD_ITEM = register(PlayInAddUnsoldItemPacket::new);
    public static final PacketType<PlayInRemoveUnsoldItemPacket> REMOVE_UNSOLD_ITEM = register(PlayInRemoveUnsoldItemPacket::new);
    public static final PacketType<PlayInUpdateUserPacket> UPDATE_USER = register(PlayInUpdateUserPacket::new);
    public static final PacketType<PlayInSendMessagePacket> SEND_MESSAGE = register(PlayInSendMessagePacket::new);
    public static final PacketType<PlayInPingRequestPacket> PING_REQUEST = register(PlayInPingRequestPacket::new);
    public static final PacketType<PlayInPingResponsePacket> PING_RESPONSE = register(PlayInPingResponsePacket::new);

    private final PacketInSuppler<T> suppler;
    private final int id;

    PacketType(PacketInSuppler<T> suppler, int id) {
        this.suppler = suppler;

        this.id = id;
    }

    private static <T extends PacketIn> PacketType<T> register(PacketInSuppler<T> suppler){
        int id = types.size();
        PacketType<T> type = new PacketType<>(suppler, id);
        types.add(type);
        return type;
    }

    public int ordinal(){
        return getId();
    }

    public int getId() {
        return id;
    }

    public static PacketType<?> byId(int id){
        return types.get(id);
    }

    public static PacketType<?>[] values() {
        return types.toArray(new PacketType[0]);
    }

    public PacketInSuppler<? extends PacketIn> getSuppler() {
        return suppler;
    }

    public interface PacketInSuppler<T extends PacketIn> {
        @NotNull
        T get(DataInputStream in) throws IOException;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PacketType<?> type)) return false;
        return id == type.id;
    }

    @Override
    public int hashCode() {
        return id;
    }
}
