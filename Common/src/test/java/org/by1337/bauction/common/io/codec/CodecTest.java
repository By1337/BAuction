package org.by1337.bauction.common.io.codec;

import org.by1337.bauction.common.db.type.SellItem;
import org.by1337.bauction.common.db.type.UnsoldItem;
import org.by1337.bauction.common.db.type.User;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.btcp.common.io.AbstractByteBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
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
                "sellerName",
                UUID.randomUUID(),
                100,
                true,
                Set.of("123", "342"),
                Long.MAX_VALUE,
                Long.MIN_VALUE,
                -1,
                0,
                100,
                "server",
                new CompoundTag()
        );
        user = new User(
                "nick",
                UUID.randomUUID(),
                9999,
                Double.MAX_VALUE,
                108,
                Long.MIN_VALUE,
                new CompoundTag()
        );
        unsoldItem = new UnsoldItem(
                new CompoundTag(),
                Long.MIN_VALUE,
                UUID.randomUUID(),
                -1,
                Long.MAX_VALUE,
                new CompoundTag()
        );
    }

    @Test
    void sellItemTest() {
        AbstractByteBuffer buffer = AbstractByteBuffer.wrap(new ByteArrayOutputStream());
        SellItem.CODEC.write(source, buffer);
        SellItem read = SellItem.CODEC.read(AbstractByteBuffer.wrap(new ByteArrayInputStream(buffer.toByteArray())), SellItemCodec.CURRENT_VERSION);
        assertEquals(source, read);
    }

    @Test
    void userTest() {
        AbstractByteBuffer buffer = AbstractByteBuffer.wrap(new ByteArrayOutputStream());
        User.CODEC.write(user, buffer);
        User read = User.CODEC.read(AbstractByteBuffer.wrap(new ByteArrayInputStream(buffer.toByteArray())), UserCodec.CURRENT_VERSION);
        assertEquals(user, read);
    }

    @Test
    void unsoldItemTest() {
        AbstractByteBuffer buffer = AbstractByteBuffer.wrap(new ByteArrayOutputStream());
        UnsoldItem.CODEC.write(unsoldItem, buffer);
        UnsoldItem read = UnsoldItem.CODEC.read(AbstractByteBuffer.wrap(new ByteArrayInputStream(buffer.toByteArray())), UnsoldItemCodec.CURRENT_VERSION);
        assertEquals(unsoldItem, read);
    }
}