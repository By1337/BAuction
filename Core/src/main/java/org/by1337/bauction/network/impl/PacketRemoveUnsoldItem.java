package org.by1337.bauction.network.impl;

import org.by1337.bauction.network.ByteBuffer;
import org.by1337.bauction.network.Packet;
import org.by1337.bauction.network.PacketType;

public class PacketRemoveUnsoldItem extends Packet {
 //   private UniqueName name;



    public PacketRemoveUnsoldItem() {
        super(PacketType.REMOVE_UNSOLD_ITEM);
    }

    @Override
    public void write(ByteBuffer buffer) {
     //   buffer.writeUtf(name.getKey());
    }

    @Override
    public void read(ByteBuffer buffer) {
       // name = new CUniqueName(buffer.readUtf());
    }

}
