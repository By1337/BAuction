package org.by1337.bauction.db.kernel;

import org.bukkit.entity.Player;
import org.by1337.bauction.db.SortingItems;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.auction.Sorting;
import org.by1337.blib.util.NameKey;
import org.by1337.blib.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Supplier;

@ThreadSafe
public abstract class SimpleDatabase {
    private static final Comparator<SellItem> SELL_ITEM_COMPARATOR = (o, o1) -> {
        int res = Long.compare(o.removalDate, o1.removalDate);
        if (res == 0) {
            return Long.compare(o.id, o1.id);
        }
        return res;
    };
    private static final Comparator<UnsoldItem> UNSOLD_ITEM_COMPARATOR = (o, o1) -> {
        int res = Long.compare(o.deleteVia, o1.deleteVia);
        if (res == 0) {
            return Long.compare(o.id, o1.id);
        }
        return res;
    };
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<UUID, User> users = new HashMap<>();
    private final TreeSet<SellItem> sortedSellItems;
    private final TreeSet<UnsoldItem> sortedUnsoldItems;

    private final Map<NameKey, Category> categoryMap;
    private final Map<NameKey, Sorting> sortingMap;
    private final Indexed indexed;

    public SimpleDatabase(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap) {
        this.categoryMap = categoryMap;
        this.sortingMap = sortingMap;
        sortedSellItems = new TreeSet<>(SELL_ITEM_COMPARATOR);
        sortedUnsoldItems = new TreeSet<>(UNSOLD_ITEM_COMPARATOR);
        indexed = new Indexed();
    }

    public int getSellItemsCount() {
        return readLock(sortedSellItems::size);
    }

    @Nullable
    public SellItem getFirstSellItem() {
        return readLock(() -> sortedSellItems.isEmpty() ? null : sortedSellItems.first());
    }

    @Nullable
    public UnsoldItem getFirstUnsoldItem() {
        return readLock(() -> sortedUnsoldItems.isEmpty() ? null : sortedUnsoldItems.first());
    }

    public boolean hasUser(UUID uuid) {
        return readLock(() -> users.containsKey(uuid));
    }

    public boolean hasSellItem(long id) {
        return readLock(() -> indexed.sellItemsMap.containsKey(id));
    }

    protected void addSellItem(@NotNull SellItem sellItem) {
        if (hasSellItem(sellItem.id)) {
            throw new IllegalStateException("SellItem with id '" + sellItem.id + "' already exists in the database!");
        }
        writeLock(() -> {
            sortedSellItems.add(sellItem);
            indexed.addSellItem(sellItem);
        });
    }

    @Nullable
    public SellItem getSellItem(long id) {
        return readLock(() -> indexed.sellItemsMap.get(id));
    }

    protected void removeSellItem(long id) {
        SellItem item = getSellItem(id);
        if (item == null) {
            throw new NoSuchElementException("Has no SellItem with id: " + id);
        }
        writeLock(() -> {
            sortedSellItems.remove(item);
            indexed.removeSellItem(item);
        });
    }

    public boolean hasUnsoldItem(long id) {
        return readLock(() -> indexed.unsoldItemsMap.containsKey(id));
    }

    @Nullable
    public UnsoldItem getUnsoldItem(long id) {
        return readLock(() -> indexed.unsoldItemsMap.get(id));
    }

    protected void removeUnsoldItem(long id) {
        UnsoldItem item = getUnsoldItem(id);
        if (item == null) {
            throw new NoSuchElementException("Has no UnsoldItem with id: " + id);
        }
        writeLock(() -> {
            sortedUnsoldItems.remove(item);
            indexed.removeUnsoldItem(item);
        });
    }

    protected void addUnsoldItem(@NotNull UnsoldItem unsoldItem) {
        if (hasUnsoldItem(unsoldItem.id)) {
            throw new IllegalStateException("UnsoldItem with id '" + unsoldItem.id + "' already exists in the database!");
        }
        writeLock(() -> {
            sortedUnsoldItems.add(unsoldItem);
            indexed.addUnsoldItem(unsoldItem);
        });
    }

    @Nullable
    public User getUser(UUID uuid) {
        return readLock(() -> users.get(uuid));
    }

    public User getUserOrCreate(Player player) {
        return getUserOrCreate(player.getName(), player.getUniqueId());
    }

    public User getUserOrCreate(String name, UUID uuid) {
        User user = getUser(uuid);
        if (user != null) return user;
        user = new User(name, uuid);
        User finalUser = user;
        writeLock(() -> users.put(uuid, finalUser));
        return user;
    }

    protected void setUser(User user) {
        writeLock(() -> users.put(user.uuid, user));
    }

    public int getUnsoldItemsCount() {
        return readLock(sortedUnsoldItems::size);
    }
    public int getUsersCount() {
        return readLock(users::size);
    }

    public void forEachUnsoldItems(Consumer<UnsoldItem> action) {
        readLock(() -> sortedUnsoldItems.forEach(action));
    }
    public void forEachUsers(Consumer<User> action) {
        readLock(() -> users.values().forEach(action));
    }

    public void forEachSellItemsBy(Consumer<SellItem> action, NameKey category, NameKey sorting) {
        readLock(() -> {
            Map<NameKey, SortingItems> map = indexed.sortedItems.get(category);
            if (map == null) {
                throw new IllegalStateException("unknown category: " + category.getName());
            }
            SortingItems sortingItems = map.get(sorting);
            if (sortingItems == null) {
                throw new IllegalStateException("unknown sorting: " + sorting.getName());
            }
            sortingItems.getItems().forEach(action);
        });
    }

    public int getCountItemsByCategory(@NotNull NameKey category) {
        return readLock(() -> {
            Map<NameKey, SortingItems> map = indexed.sortedItems.get(category);
            if (map == null) {
                throw new IllegalStateException("unknown category: " + category.getName());
            }
            for (SortingItems value : map.values()) {
                return value.size();
            }
            return 0;
        });
    }

    public void forEachSellItems(Consumer<SellItem> action) {
        readLock(() -> sortedSellItems.forEach(action));
    }

    public void forEachSellItemsByUser(Consumer<SellItem> action, @NotNull UUID uuid) {
        readLock(() -> {
            Pair<TreeSet<SellItem>, TreeSet<UnsoldItem>> pair = indexed.itemsByOwner.get(uuid);
            if (pair == null) return;
            pair.getKey().forEach(action);
        });
    }

    public void forEachUnsoldItemsByUser(Consumer<UnsoldItem> action, @NotNull UUID uuid) {
        readLock(() -> {
            Pair<TreeSet<SellItem>, TreeSet<UnsoldItem>> pair = indexed.itemsByOwner.get(uuid);
            if (pair == null) return;
            pair.getRight().forEach(action);
        });
    }

    public int getSellItemsCountByUser(@NotNull UUID uuid) {
        return readLock(() -> {
            Pair<TreeSet<SellItem>, TreeSet<UnsoldItem>> pair = indexed.itemsByOwner.get(uuid);
            if (pair == null) return 0;
            return pair.getKey().size();
        });
    }

    public int getUnsoldItemsCountByUser(@NotNull UUID uuid) {
        return readLock(() -> {
            Pair<TreeSet<SellItem>, TreeSet<UnsoldItem>> pair = indexed.itemsByOwner.get(uuid);
            if (pair == null) return 0;
            return pair.getRight().size();
        });
    }


    private class Indexed {
        private final Map<Long, SellItem> sellItemsMap = new HashMap<>();
        private final Map<Long, UnsoldItem> unsoldItemsMap = new HashMap<>();
        private final Map<UUID, Pair<TreeSet<SellItem>, TreeSet<UnsoldItem>>> itemsByOwner = new HashMap<>();
        private final Map<NameKey, Map<NameKey, SortingItems>> sortedItems = new HashMap<>();

        public Indexed() {
            writeLock(() -> {
                for (Category category : categoryMap.values()) {
                    Map<NameKey, SortingItems> map = new HashMap<>();
                    for (Sorting sorting : sortingMap.values()) {
                        map.put(sorting.nameKey(), new SortingItems(sorting));
                    }
                    sortedItems.put(category.nameKey(), map);
                }
            });
        }

        private void addUnsoldItem(@NotNull UnsoldItem unsoldItem) {
            isWriteLock();
            unsoldItemsMap.put(unsoldItem.id, unsoldItem);
            Pair<TreeSet<SellItem>, TreeSet<UnsoldItem>> pair = itemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k ->
                    new Pair<>(new TreeSet<>(SELL_ITEM_COMPARATOR), new TreeSet<>(UNSOLD_ITEM_COMPARATOR))
            );
            pair.getRight().add(unsoldItem);
        }

        private void removeUnsoldItem(@NotNull UnsoldItem unsoldItem) {
            isWriteLock();
            unsoldItemsMap.remove(unsoldItem.id);
            Pair<TreeSet<SellItem>, TreeSet<UnsoldItem>> pair = itemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k ->
                    new Pair<>(new TreeSet<>(SELL_ITEM_COMPARATOR), new TreeSet<>(UNSOLD_ITEM_COMPARATOR))
            );
            pair.getRight().remove(unsoldItem);
            if (pair.getLeft().isEmpty() && pair.getRight().isEmpty()) {
                itemsByOwner.remove(unsoldItem.sellerUuid);
            }
        }

        private void removeSellItem(@NotNull SellItem sellItem) {
            isWriteLock();
            sellItemsMap.remove(sellItem.id);
            Pair<TreeSet<SellItem>, TreeSet<UnsoldItem>> pair = itemsByOwner.computeIfAbsent(sellItem.sellerUuid, k ->
                    new Pair<>(new TreeSet<>(SELL_ITEM_COMPARATOR), new TreeSet<>(UNSOLD_ITEM_COMPARATOR))
            );
            pair.getLeft().remove(sellItem);
            if (pair.getLeft().isEmpty() && pair.getRight().isEmpty()) {
                itemsByOwner.remove(sellItem.sellerUuid);
            }
            sortedItems.forEach((key, value) -> {
                Category category = categoryMap.get(key);
                if (category == null) throw new IllegalStateException("unknown category: " + key);
                if (category.matches(sellItem)) {
                    value.values().forEach(sort -> sort.remove(sellItem));
                }
            });
        }

        private void addSellItem(@NotNull SellItem sellItem) {
            isWriteLock();
            sellItemsMap.put(sellItem.id, sellItem);
            Pair<TreeSet<SellItem>, TreeSet<UnsoldItem>> pair = itemsByOwner.computeIfAbsent(sellItem.sellerUuid, k ->
                    new Pair<>(new TreeSet<>(SELL_ITEM_COMPARATOR), new TreeSet<>(UNSOLD_ITEM_COMPARATOR))
            );
            pair.getLeft().add(sellItem);
            sortedItems.forEach((key, value) -> {
                Category category = categoryMap.get(key);
                if (category == null) throw new IllegalStateException("unknown category: " + key);
                if (category.matches(sellItem)) {
                    value.values().forEach(sort -> sort.addItem(sellItem));
                }
            });
        }
    }

    protected void isWriteLock() {
        if (!lock.isWriteLockedByCurrentThread()) {
            throw new IllegalStateException("Current thread does not hold the write lock");
        }
    }

    protected <T> T writeLock(Supplier<T> task) {
        lock.writeLock().lock();
        try {
            return task.get();
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected void writeLock(Runnable task) {
        lock.writeLock().lock();
        try {
            task.run();
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected <T> T readLock(Supplier<T> task) {
        lock.readLock().lock();
        try {
            return task.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    protected void readLock(Runnable task) {
        lock.readLock().lock();
        try {
            task.run();
        } finally {
            lock.readLock().unlock();
        }
    }
}
