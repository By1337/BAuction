package org.by1337.bauction.network;

import org.by1337.bauction.network.impl.*;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class PacketType<T extends Packet> {
    private static final List<PacketType<?>> types = new ArrayList<>();

    public static final PacketType<PacketAddSellItem> ADD_SELL_ITEM = register(PacketAddSellItem::new);
    public static final PacketType<PacketRemoveSellItem> REMOVE_SELL_ITEM = register(PacketRemoveSellItem::new);
    public static final PacketType<PacketAddUnsoldItem> ADD_UNSOLD_ITEM = register(PacketAddUnsoldItem::new);
    public static final PacketType<PacketRemoveUnsoldItem> REMOVE_UNSOLD_ITEM = register(PacketRemoveUnsoldItem::new);
    public static final PacketType<PacketUpdateUser> UPDATE_USER = register(PacketUpdateUser::new);
    public static final PacketType<PacketSendMessage> SEND_MESSAGE = register(PacketSendMessage::new);
    public static final PacketType<PacketPingRequest> PING_REQUEST = register(PacketPingRequest::new);
    public static final PacketType<PacketPingResponse> PING_RESPONSE = register(PacketPingResponse::new);
    public static final PacketType<PacketGiveMoneyRequest> GIVE_MONEY_REQUEST = register(PacketGiveMoneyRequest::new);
    public static final PacketType<PacketGiveMoneyResponse> GIVE_MONEY_RESPONSE = register(PacketGiveMoneyResponse::new);

    private final Supplier<T> suppler;
    private final int id;

    PacketType(Supplier<T> suppler, int id) {
        this.suppler = suppler;
        this.id = id;
    }

    private static <T extends Packet> PacketType<T> register(Supplier<T> suppler){
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

    public Supplier<? extends Packet> getSuppler() {
        return suppler;
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
