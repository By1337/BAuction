package org.by1337.bauction.db.kernel.v2;

import net.kyori.adventure.text.Component;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;
import org.by1337.bauction.db.v2.AddSellItemEvent;
import org.by1337.bauction.db.v2.Event;
import org.by1337.bauction.db.v2.EventPipeline;
import org.by1337.bauction.db.v2.TakeSellItemEvent;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.auction.Sorting;
import org.by1337.bauction.util.threading.ThreadCreator;
import org.by1337.blib.util.NameKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.*;


public class LocalDatabase extends SimpleDatabase {
    private static final Logger LOGGER = LoggerFactory.getLogger("BAuction");
    private final ThreadFactory threadFactory;
    private final ThreadPoolExecutor executor;
    private final EventPipeline<Event> pipeline;

    public LocalDatabase(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap) {
        super(categoryMap, sortingMap);
        threadFactory = ThreadCreator.createWithName("bauc-db-worker-#d");

        executor = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors() / 2,
                60L, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                threadFactory);
        pipeline = new EventPipeline<>();
        pipeline.addLast("base_addSellItemEvent", AddSellItemEvent.class, this::addSellItemEvent);
        pipeline.addLast("base_takeSellItemEvent", TakeSellItemEvent.class, this::takeSellItemEvent);
    }

    public <T extends Event> CompletableFuture<T> onEvent(T event) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                pipeline.run(event);
            } catch (Throwable t) {
                LOGGER.error("An error occurred during pipeline execution!", t);
                if (event.isValid()) {
                    event.setValid(false);
                    event.setReason(Component.text("An internal error occurred")); // todo lang file
                }
            }
            return event;
        }, executor);
    }

    private void takeSellItemEvent(TakeSellItemEvent event) {
        SellItem sellItem = event.getSellItem();
        User user = event.getUser();
        if (sellItem.sellerUuid != user.uuid) {
            event.setValid(false);
            event.setReason(Component.text("You do not own this item!")); // todo lang file
            return;
        }
        try {
            removeSellItem(sellItem.id);
        } catch (NoSuchElementException e) {
            event.setValid(false);
            event.setReason(Component.text("This item no longer exists")); // todo lang file
        }catch (Throwable t){
            event.setValid(false);
            event.setReason(Component.text("An internal error occurred")); // todo lang file
            LOGGER.error("An error occurred while trying to delete SellItem item", t);
        }
    }

    private void addSellItemEvent(AddSellItemEvent event) {
        SellItem sellItem = event.getSellItem();
        User user = event.getUser();
        if (getSellItemsCountByUser(user.getUuid()) <= user.getMaxItems()) {
            event.setValid(false);
            event.setReason(Component.text("items count limit!")); // todo lang file
            return;
        }
        try {
            addSellItem(sellItem);
        } catch (Throwable t) {
            event.setValid(false);
            event.setReason(Component.text("An internal error occurred")); // todo lang file
            LOGGER.error("An error occurred while trying to add a new SellItem to the database", t);
        }
    }

    protected void load() {

    }

    public EventPipeline<Event> getPipeline() {
        return pipeline;
    }
}
