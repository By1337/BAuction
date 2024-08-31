package org.by1337.bauction.db.kernel;

import org.bukkit.Material;
import org.by1337.bauction.test.util.BLibApi;
import org.by1337.bauction.Main;
import org.by1337.bauction.PluginBootstrap;
import org.by1337.bauction.db.kernel.event.*;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class LocalDatabaseTest {
    private MemoryDatabase database;
    private User seller;
    private User buyer;
    private Main plugin;
    private PluginBootstrap pluginBootstrap;

    @BeforeEach
    void setUp() {
        pluginBootstrap = new PluginBootstrap();
        pluginBootstrap.init();

        database = Main.getStorage();
        seller = database.getUserOrCreate("seller", UUID.randomUUID());
        buyer = database.getUserOrCreate("buyer", UUID.randomUUID());
        BLibApi.setApi();
    }

    @Test
    void runTests() {
        sellItemTest();
        buyItemTest();
        buyItemCountTest();
        takeItemTest();
        addAndTakeUnsoldItemTest();
    }

    void sellItemTest() {
        SellItem sellItem = createSellItem();

        AddSellItemEvent event = new AddSellItemEvent(sellItem, seller);

        database.onEvent(event).join();

        assertTrue(event.isValid());

        assertEquals(sellItem, database.getFirstSellItem());
        assertEquals(1, database.getSellItemsCount());
    }

    void buyItemTest() {
        BuyItemEvent event = new BuyItemEvent(buyer, Objects.requireNonNull(database.getFirstSellItem()));

        database.onEvent(event).join();

        assertTrue(event.isValid());

        assertEquals(0, database.getSellItemsCount());
    }

    void buyItemCountTest() {
        SellItem sellItem = createSellItem();
        assertTrue(database.onEvent(new AddSellItemEvent(sellItem, seller)).join().isValid());

        BuyCountItemEvent event = database.onEvent(new BuyCountItemEvent(buyer, sellItem, 15)).join();

        assertTrue(event.isValid());

        assertEquals(64 - 15, Objects.requireNonNull(database.getFirstSellItem()).amount);

        database.removeSellItem(database.getFirstSellItem().id);
    }
    void takeItemTest() {
        SellItem sellItem = createSellItem();
        assertTrue(database.onEvent(new AddSellItemEvent(sellItem, seller)).join().isValid());

        TakeItemEvent event = database.onEvent(new TakeItemEvent(sellItem, seller)).join();

        assertTrue(event.isValid());

        assertEquals(0, database.getSellItemsCount());
    }
    void addAndTakeUnsoldItemTest() {
        UnsoldItem unsoldItem = createUnsoldItem();
        assertTrue(database.onEvent(new AddUnsoldItemEvent(unsoldItem)).join().isValid());

        TakeUnsoldItemEvent event = database.onEvent(new TakeUnsoldItemEvent(seller, unsoldItem)).join();

        assertTrue(event.isValid());

        assertEquals(0, database.getUnsoldItemsCount());
    }

    private SellItem createSellItem() {
        return new SellItem(
                new CompoundTag(),
                seller.nickName,
                seller.uuid,
                100,
                true,
                Set.of("123"),
                Long.MAX_VALUE,
                Long.MAX_VALUE,
                Main.getUniqueIdGenerator().nextId(),
                Material.AIR,
                64,
                10,
                null,
                "server",
                new CompoundTag()
        );
    }
    private UnsoldItem createUnsoldItem(){
        return new UnsoldItem(
                new CompoundTag(),
                Long.MAX_VALUE,
                seller.uuid,
                Main.getUniqueIdGenerator().nextId(),
                Long.MAX_VALUE,
                new CompoundTag()
        );
    }

    @AfterEach
    void tearDown() {
        pluginBootstrap.close();
    }
}