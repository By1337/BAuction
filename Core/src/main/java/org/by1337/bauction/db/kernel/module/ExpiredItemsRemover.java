package org.by1337.bauction.db.kernel.module;

import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.DatabaseModule;
import org.by1337.bauction.db.kernel.MemoryDatabase;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.UnsoldItem;
import org.by1337.bauction.db.kernel.event.*;
import org.by1337.bauction.util.threading.ThreadCreator;
import org.by1337.bauction.util.time.TimeParser;
import org.by1337.blib.nbt.impl.CompoundTag;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExpiredItemsRemover implements DatabaseModule, Closeable {
    private static final long FOUR_TICKS = 200;
    private static final long TEN_SECONDS = 10 * 1000;
    private static final long THIRTY_SECONDS = TEN_SECONDS * 3;
    private MemoryDatabase database;
    private final ScheduledExecutorService scheduler;
    private final boolean removeUnsoldItems;
    private final long removeTime;

    public ExpiredItemsRemover() {
        scheduler = new ScheduledThreadPoolExecutor(1, ThreadCreator.createWithName("bauc-expired-items-remover-#%d"));
        removeUnsoldItems = Main.getCfg().getConfig().getAsBoolean("remove-expired-items.enable", false);
        removeTime = TimeParser.parse(Main.getCfg().getConfig().getAsString("remove-expired-items.time", "2d"));
    }

    @Override
    public void preLoad(EventPipeline<Event> pipeline, MemoryDatabase database) {
        this.database = database;
    }

    @Override
    public void postLoad() {
        tick();
    }

    private ScheduledFuture<?> task;

    private void tick() {
        long wait = THIRTY_SECONDS;
        wait = Math.min(wait, removeSellItems());
        if (removeUnsoldItems) {
            wait = Math.min(wait, removeUnsoldItems());
        }
        task = scheduler.schedule(this::tick, wait, TimeUnit.MILLISECONDS);
    }

    private long removeSellItems() {
        SellItem sellItem = database.getFirstSellItem();
        if (sellItem != null) {
            long remove = sellItem.removalDate - System.currentTimeMillis();
            if (remove <= 0) {
                database.onEvent(new RemoveSellItemEvent(sellItem)).thenAccept(event -> {
                    if (event.isValid()) {
                        database.onEvent(new AddUnsoldItemEvent(
                                new UnsoldItem(
                                        event.getSellItem().item,
                                        System.currentTimeMillis(),
                                        event.getSellItem().sellerUuid,
                                        Main.getUniqueIdGenerator().nextId(),
                                        System.currentTimeMillis() + removeTime,
                                        new CompoundTag()
                                ))
                        );
                    } else {
                        LOGGER.warn(
                                "[ExpiredItemsRemover] I deleted SellItem {} and tried to add UnsoldItem but the database won't let me do it because {}",
                                sellItem.compactToString(),
                                event.getReason()
                        );
                    }
                });
                return FOUR_TICKS;
            } else {
                return remove;
            }
        }
        return TEN_SECONDS;
    }

    private long removeUnsoldItems() {
        UnsoldItem unsoldItem = database.getFirstUnsoldItem();
        if (unsoldItem != null) {
            long remove = unsoldItem.deleteVia;
            if (remove <= 0) {
                database.onEvent(new RemoveUnsoldItemEvent(unsoldItem));
                return FOUR_TICKS;
            } else {
                return remove;
            }
        }
        return TEN_SECONDS;
    }

    @Override
    public void close() throws IOException {
        try {
            if (task != null) {
                task.cancel(false);
            }
        } finally {
            scheduler.shutdown();
        }
    }
}
