package org.by1337.bauction.db.io.codec;

import org.bukkit.Material;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.util.id.CUniqueName;
import org.by1337.blib.io.ByteBuffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SellItemCodecTest {
    private SellItem source;

    @BeforeEach
    public void setUp() {
        source = new SellItem(
                "item",
                "_By1337_",
                UUID.randomUUID(),
                Double.MAX_VALUE,
                false,
                Set.of("tag", "tag2", "tag3"),
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                new CUniqueName("key"),
                Material.AIR,
                127,
                Double.MAX_VALUE / 127,
                Set.of(),
                null,
                "server",
                false
        );
    }

    @Test
    void toByteArrTest() {
        ByteBuffer buffer = new ByteBuffer();
        SellItem.CODEC.write(source, buffer);
        SellItem read = SellItem.CODEC.read(new ByteBuffer(buffer.toByteArray()), SellItemCodec.CURRENT_VERSION);
        assertEquals(source, read);
    }

}