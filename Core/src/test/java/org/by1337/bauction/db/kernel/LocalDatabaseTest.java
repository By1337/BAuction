package org.by1337.bauction.db.kernel;

import org.bukkit.Material;
import org.by1337.bauction.common.db.type.SellItem;
import org.by1337.bauction.common.db.type.UnsoldItem;
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
    private PluginUser seller;
    private PluginUser buyer;
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
        PluginSellItem sellItem = createSellItem();

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
        PluginSellItem sellItem = createSellItem();
        assertTrue(database.onEvent(new AddSellItemEvent(sellItem, seller)).join().isValid());

        BuyCountItemEvent event = database.onEvent(new BuyCountItemEvent(buyer, sellItem, 15)).join();

        assertTrue(event.isValid());

        assertEquals(64 - 15, Objects.requireNonNull(database.getFirstSellItem()).getAmount());

        database.removeSellItem(database.getFirstSellItem().getId());
    }
    void takeItemTest() {
        PluginSellItem sellItem = createSellItem();
        assertTrue(database.onEvent(new AddSellItemEvent(sellItem, seller)).join().isValid());

        TakeItemEvent event = database.onEvent(new TakeItemEvent(sellItem, seller)).join();

        assertTrue(event.isValid());

        assertEquals(0, database.getSellItemsCount());
    }
    void addAndTakeUnsoldItemTest() {
        PluginUnsoldItem unsoldItem = createUnsoldItem();
        assertTrue(database.onEvent(new AddUnsoldItemEvent(unsoldItem)).join().isValid());

        TakeUnsoldItemEvent event = database.onEvent(new TakeUnsoldItemEvent(seller, unsoldItem)).join();

        assertTrue(event.isValid());

        assertEquals(0, database.getUnsoldItemsCount());
    }

    private PluginSellItem createSellItem() {
        return new PluginSellItem(
               new SellItem(
                       new CompoundTag(),
                       seller.getNickName(),
                       seller.getUuid(),
                       100,
                       true,
                       Set.of("123"),
                       Long.MAX_VALUE,
                       Long.MAX_VALUE,
                       Main.getUniqueIdGenerator().nextId(),
                       0,
                       64,
                       "server",
                       new CompoundTag()
               )
        );
    }
    private PluginUnsoldItem createUnsoldItem(){
        return new PluginUnsoldItem(
               new UnsoldItem(
                       new CompoundTag(),
                       Long.MAX_VALUE,
                       seller.getUuid(),
                       Main.getUniqueIdGenerator().nextId(),
                       Long.MAX_VALUE,
                       new CompoundTag()
               )
        );
    }

    @AfterEach
    void tearDown() {
        pluginBootstrap.close();
    }
}