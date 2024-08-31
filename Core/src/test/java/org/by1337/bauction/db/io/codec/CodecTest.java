
package org.by1337.bauction.db.io.codec;

import org.bukkit.Material;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.UnsoldItem;
import org.by1337.bauction.db.kernel.User;
import org.by1337.blib.io.ByteBuffer;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CodecTest {
    private SellItem source;
    private User user;
    private UnsoldItem unsoldItem;

    @BeforeEach
    public void setUp() {
        source = new SellItem(
                new CompoundTag(),
                "seller",
                UUID.randomUUID(),
                100,
                false,
                Set.of("tag"),
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                0,
                Material.AIR,
                64,
                1.5625,
                null,
                "server",
                new CompoundTag()
        );
        user = new User(
                "nick",
                UUID.randomUUID(),
                99,
                123D,
                new CompoundTag());
        unsoldItem = new UnsoldItem(
                new CompoundTag(),
                Long.MAX_VALUE,
                UUID.randomUUID(),
                0,
                Long.MAX_VALUE,
                new CompoundTag()
        );

    }

    @Test
    void sellItemTest() {
        ByteBuffer buffer = new ByteBuffer();
        SellItem.CODEC.write(source, buffer);
        SellItem read = SellItem.CODEC.read(new ByteBuffer(buffer.toByteArray()), SellItemCodec.CURRENT_VERSION);
        assertEquals(source, read);
    }

    @Test
    void userTest() {
        ByteBuffer buffer = new ByteBuffer();
        User.CODEC.write(user, buffer);
        User read = User.CODEC.read(new ByteBuffer(buffer.toByteArray()), UserCodec.CURRENT_VERSION);
        assertEquals(user, read);
    }

    @Test
    void unsoldItemTest() {
        ByteBuffer buffer = new ByteBuffer();
        UnsoldItem.CODEC.write(unsoldItem, buffer);
        UnsoldItem read = UnsoldItem.CODEC.read(new ByteBuffer(buffer.toByteArray()), UnsoldItemCodec.CURRENT_VERSION);
        assertEquals(unsoldItem, read);
    }
}
