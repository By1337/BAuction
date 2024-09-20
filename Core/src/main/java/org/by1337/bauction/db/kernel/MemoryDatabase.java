package org.by1337.bauction.db.kernel;

import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.common.db.event.Event;
import org.by1337.bauction.common.db.event.EventPipeline;
import org.by1337.bauction.common.db.type.SellItem;
import org.by1337.bauction.db.kernel.event.*;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.auction.Sorting;
import org.by1337.bauction.util.threading.ThreadCreator;
import org.by1337.blib.nbt.CompressedNBT;
import org.by1337.blib.nbt.impl.ByteArrNBT;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.blib.util.NameKey;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.*;


public class MemoryDatabase extends SimpleDatabase implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger("BAuction");
    protected final ThreadFactory threadFactory;
    protected final ThreadPoolExecutor executor;
    protected final EventPipeline<Event> pipeline;
    protected final List<DatabaseModule> modules;

    public MemoryDatabase(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap, List<DatabaseModule> modules) {
        super(categoryMap, sortingMap);
        this.modules = modules;

        threadFactory = ThreadCreator.createWithName("bauc-db-worker-#%d");

        executor = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors() / 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                threadFactory);
        pipeline = new EventPipeline<>();
        pipeline.addLast("base_addSellItemEvent", AddSellItemEvent.class, this::addSellItemEventHandler);
        pipeline.addLast("base_takeItemEvent", TakeItemEvent.class, this::takeItemEventHandler);
        pipeline.addLast("base_buyItemEvent", BuyItemEvent.class, this::buyItemEventHandler);
        pipeline.addLast("base_buyCountItemEvent", BuyCountItemEvent.class, this::buyCountItemEventHandler);
        pipeline.addLast("base_takeUnsoldItemEvent", TakeUnsoldItemEvent.class, this::takeUnsoldItemEventHandler);
        pipeline.addLast("base_addUnsoldItemEvent", AddUnsoldItemEvent.class, this::addUnsoldItemEventHandler);

        pipeline.addLast("base_removeSellItemEvent", RemoveSellItemEvent.class, this::removeSellItemHandler);
        pipeline.addLast("base_removeUnsoldItemEvent", RemoveUnsoldItemEvent.class, this::removeUnsoldItemHandler);
        modules.forEach(m -> m.preLoad(pipeline, this));

        modules.forEach(DatabaseModule::postLoad);
    }

    public <T extends Event> CompletableFuture<T> onEvent(T event) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                pipeline.run(event);
            } catch (Throwable t) {
                LOGGER.error("An error occurred during pipeline execution!", t);
                if (event.isValid()) {
                    event.setValid(false);
                    event.setReason("<red>An internal error occurred"); // todo lang file
                }
            }
            return event;
        }, executor);
    }

    private void addUnsoldItemEventHandler(AddUnsoldItemEvent event) {
        PluginUnsoldItem unsoldItem = event.getUnsoldItem();
        try {
            addUnsoldItem(unsoldItem);
        } catch (Throwable t) {
            event.setValid(false);
            event.setReason("<red>An internal error occurred"); // todo lang file
            LOGGER.error("An error occurred while trying to add PluginUnsoldItem item", t);
        }
    }

    private void takeUnsoldItemEventHandler(TakeUnsoldItemEvent event) {
        PluginUnsoldItem unsoldItem = event.getUnsoldItem();
        PluginUser user = event.getTaker();
        if (!user.getUuid().equals(unsoldItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("You can't take someone else's item!"); // todo lang file
            return;
        }
        removeUnsoldItemHandler(event);
    }

    private void buyCountItemEventHandler(BuyCountItemEvent event) {
        PluginSellItem sellItem = event.getSellItem();
        PluginUser user = event.getBuyer();
        int count = event.getCount();
        @Nullable PluginUser itemOwner = getUser(sellItem.getSellerUuid());
        if (user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("You can't buy your item!"); // todo lang file
            return;
        }
        if (sellItem.getAmount() < count) {
            event.setValid(false);
            event.setReason("You are trying to buy more items than are available for sale. Available: " + sellItem.getAmount() + "."); // todo lang file
            return;
        }
        if (!sellItem.isSaleByThePiece()) {
            event.setValid(false);
            event.setReason("This item is not sold by the piece!"); // todo lang file
            return;
        }
        try {
            removeSellItem(sellItem.getId());
            int residue = sellItem.getAmount() - count;
            if (residue > 0) {
                ItemStack itemStack = sellItem.getItemStack();
                itemStack.setAmount(residue);

                SellItem result = SellItem
                        .builder(sellItem.getSource())
                        .id(Main.getUniqueIdGenerator().nextId())
                        .amount(residue)
                        .price(sellItem.getPriceForOne() * residue)
                        .item(PluginSellItem.serializeItemStack(itemStack))
                        .build();

                addSellItem(new PluginSellItem(result));
            }

        } catch (NoSuchElementException e) {
            event.setValid(false);
            event.setReason("This item no longer exists"); // todo lang file
            return;
        } catch (Throwable t) {
            event.setValid(false);
            event.setReason("<red>An internal error occurred"); // todo lang file
            LOGGER.error("An error occurred while trying to update PluginSellItem item", t);
            return;
        }
//        user.dealCount++; // todo new stats system?
//        user.dealSum += sellItem.price;
//        if (itemOwner != null) {
//            itemOwner.dealCount++;
//            itemOwner.dealSum += sellItem.price;
//        }

    }

    private void buyItemEventHandler(BuyItemEvent event) {
        PluginSellItem sellItem = event.getSellItem();
        PluginUser user = event.getBuyer();
        @Nullable PluginUser itemOwner = getUser(sellItem.getSellerUuid());
        if (user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("You can't buy your item!"); // todo lang file
            return;
        }
        removeSellItemHandler(event);
        if (event.isValid()) {
//            user.dealCount++; // todo new stats system?
//            user.dealSum += sellItem.price;
//            if (itemOwner != null) {
//                itemOwner.dealCount++;
//                itemOwner.dealSum += sellItem.price;
//            }
        }
    }

    private void removeUnsoldItemHandler(UnsoldItemEvent event) {
        PluginUnsoldItem unsoldItem = event.getUnsoldItem();
        try {
            removeUnsoldItem(unsoldItem.getId());
        } catch (NoSuchElementException e) {
            event.setValid(false);
            event.setReason("This item no longer exists"); // todo lang file
        } catch (Throwable t) {
            event.setValid(false);
            event.setReason("<red>An internal error occurred"); // todo lang file
            LOGGER.error("An error occurred while trying to delete PluginUnsoldItem item", t);
        }
    }

    private void removeSellItemHandler(SellItemEvent event) {
        PluginSellItem sellItem = event.getSellItem();
        try {
            removeSellItem(sellItem.getId());
        } catch (NoSuchElementException e) {
            event.setValid(false);
            event.setReason("This item no longer exists"); // todo lang file
        } catch (Throwable t) {
            event.setValid(false);
            event.setReason("<red>An internal error occurred"); // todo lang file
            LOGGER.error("An error occurred while trying to delete PluginSellItem item", t);
        }
    }

    private void takeItemEventHandler(TakeItemEvent event) {
        PluginSellItem sellItem = event.getSellItem();
        PluginUser user = event.getUser();
        if (!sellItem.getSellerUuid().equals(user.getUuid())) {
            event.setValid(false);
            event.setReason("You do not own this item!"); // todo lang file
            return;
        }
        removeSellItemHandler(event);
    }

    private void addSellItemEventHandler(AddSellItemEvent event) {
        PluginSellItem sellItem = event.getSellItem();
        PluginUser user = event.getUser();
        if (getSellItemsCountByUser(user.getUuid()) >= user.getMaxSellItems()) {
            event.setValid(false);
            event.setReason("items count limit!"); // todo lang file
            return;
        }
        if (CompoundTag.getSizeInBytes(sellItem.getItem()) > Main.getCfg().getItemMaxSize()) {
            event.setValid(false);
            event.setReason("item too large"); // todo lang file
            return;
        } else if (sellItem.getItem() instanceof ByteArrNBT arrNBT) {
            int size = CompoundTag.getSizeInBytes(new CompressedNBT(arrNBT.getValue()).decompress());
            if (size > Main.getCfg().getMaximumUncompressedItemSize()) {
                event.setValid(false);
                event.setReason("item too large"); // todo lang file
                return;
            }
        }
        try {
            addSellItem(sellItem);
        } catch (Throwable t) {
            event.setValid(false);
            event.setReason("<red>An internal error occurred"); // todo lang file
            LOGGER.error("An error occurred while trying to add a new PluginSellItem to the database", t);
        }
    }

    public void load() {

    }

    @Override
    public void close() {
        try {
            executor.shutdown();
        } finally {
            for (DatabaseModule module : modules) {
                if (module instanceof Closeable closeable) {
                    try {
                        closeable.close();
                    } catch (Throwable t) {
                        LOGGER.error("Failed to close DatabaseModule " + module.getClass().getCanonicalName(), t);
                    }
                }
            }
        }
    }

    @VisibleForTesting
    EventPipeline<Event> getPipeline() {
        return pipeline;
    }
}
