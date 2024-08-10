package org.by1337.bauction.network.impl;

import org.by1337.bauction.network.ByteBuffer;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

public class PacketRemoveSellItem extends Packet {
    private long id;

    public PacketRemoveSellItem(long id) {
        super(PacketType.REMOVE_SELL_ITEM);
        this.id = id;
    }

    public PacketRemoveSellItem() {
        super(PacketType.REMOVE_SELL_ITEM);
    }

    @Override
    public void write(ByteBuffer buffer) {
      //  buffer.writeVarInt(id);
    }

    @Override
    public void read(ByteBuffer buffer) {
     //   name = new CUniqueName(buffer.readUtf());
    }


  /*  public UniqueName getName() {
        return name;
    }*/
}
