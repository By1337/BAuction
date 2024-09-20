package org.by1337.bauction.db.kernel;

import org.bukkit.entity.Player;
import org.by1337.bauction.common.db.type.User;
import org.by1337.bauction.db.SortingItems;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.auction.Sorting;
import org.by1337.blib.nbt.impl.CompoundTag;
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
    private static final Comparator<PluginSellItem> SELL_ITEM_COMPARATOR = (o, o1) -> {
        int res = Long.compare(o.getRemovalDate(), o1.getRemovalDate());
        if (res == 0) {
            return Long.compare(o.getId(), o1.getId());
        }
        return res;
    };
    private static final Comparator<PluginUnsoldItem> UNSOLD_ITEM_COMPARATOR = (o, o1) -> {
        int res = Long.compare(o.getDeleteVia(), o1.getDeleteVia());
        if (res == 0) {
            return Long.compare(o.getId(), o1.getId());
        }
        return res;
    };
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<UUID, PluginUser> users = new HashMap<>();
    private final TreeSet<PluginSellItem> sortedSellItems;
    private final TreeSet<PluginUnsoldItem> sortedUnsoldItems;

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
    public PluginSellItem getFirstSellItem() {
        return readLock(() -> sortedSellItems.isEmpty() ? null : sortedSellItems.first());
    }

    @Nullable
    public PluginUnsoldItem getFirstUnsoldItem() {
        return readLock(() -> sortedUnsoldItems.isEmpty() ? null : sortedUnsoldItems.first());
    }

    public boolean hasUser(UUID uuid) {
        return readLock(() -> users.containsKey(uuid));
    }

    public boolean hasSellItem(long id) {
        return readLock(() -> indexed.sellItemsMap.containsKey(id));
    }

    protected void addSellItem(@NotNull PluginSellItem sellItem) {
        if (hasSellItem(sellItem.getId())) {
            throw new IllegalStateException("PluginSellItem with id '" + sellItem.getId() + "' already exists in the database!");
        }
        writeLock(() -> {
            sortedSellItems.add(sellItem);
            indexed.addSellItem(sellItem);
        });
    }

    @Nullable
    public PluginSellItem getSellItem(long id) {
        return readLock(() -> indexed.sellItemsMap.get(id));
    }

    protected void removeSellItem(long id) {
        PluginSellItem item = getSellItem(id);
        if (item == null) {
            throw new NoSuchElementException("Has no PluginSellItem with id: " + id);
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
    public PluginUnsoldItem getUnsoldItem(long id) {
        return readLock(() -> indexed.unsoldItemsMap.get(id));
    }

    protected void removeUnsoldItem(long id) {
        PluginUnsoldItem item = getUnsoldItem(id);
        if (item == null) {
            throw new NoSuchElementException("Has no PluginUnsoldItem with id: " + id);
        }
        writeLock(() -> {
            sortedUnsoldItems.remove(item);
            indexed.removeUnsoldItem(item);
        });
    }

    protected void addUnsoldItem(@NotNull PluginUnsoldItem unsoldItem) {
        if (hasUnsoldItem(unsoldItem.getId())) {
            throw new IllegalStateException("PluginUnsoldItem with id '" + unsoldItem.getId() + "' already exists in the database!");
        }
        writeLock(() -> {
            sortedUnsoldItems.add(unsoldItem);
            indexed.addUnsoldItem(unsoldItem);
        });
    }

    @Nullable
    public PluginUser getUser(UUID uuid) {
        return readLock(() -> users.get(uuid));
    }

    public PluginUser getUserOrCreate(Player player) {
        return getUserOrCreate(player.getName(), player.getUniqueId());
    }

    public PluginUser getUserOrCreate(String name, UUID uuid) {
        PluginUser user = getUser(uuid);
        if (user != null) return user;
        user = new PluginUser(new User(name, uuid, 0, 0, 0, 0, new CompoundTag()));
        PluginUser finalUser = user;
        writeLock(() -> users.put(uuid, finalUser));
        return user;
    }

    protected void setUser(PluginUser user) {
        writeLock(() -> users.put(user.getUuid(), user));
    }

    public int getUnsoldItemsCount() {
        return readLock(sortedUnsoldItems::size);
    }
    public int getUsersCount() {
        return readLock(users::size);
    }

    public void forEachUnsoldItems(Consumer<PluginUnsoldItem> action) {
        readLock(() -> sortedUnsoldItems.forEach(action));
    }
    public void forEachUsers(Consumer<PluginUser> action) {
        readLock(() -> users.values().forEach(action));
    }

    public void forEachSellItemsBy(Consumer<PluginSellItem> action, NameKey category, NameKey sorting) {
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

    public void forEachSellItems(Consumer<PluginSellItem> action) {
        readLock(() -> sortedSellItems.forEach(action));
    }

    public void forEachSellItemsByUser(Consumer<PluginSellItem> action, @NotNull UUID uuid) {
        readLock(() -> {
            Pair<TreeSet<PluginSellItem>, TreeSet<PluginUnsoldItem>> pair = indexed.itemsByOwner.get(uuid);
            if (pair == null) return;
            pair.getKey().forEach(action);
        });
    }

    public void forEachUnsoldItemsByUser(Consumer<PluginUnsoldItem> action, @NotNull UUID uuid) {
        readLock(() -> {
            Pair<TreeSet<PluginSellItem>, TreeSet<PluginUnsoldItem>> pair = indexed.itemsByOwner.get(uuid);
            if (pair == null) return;
            pair.getRight().forEach(action);
        });
    }

    public int getSellItemsCountByUser(@NotNull UUID uuid) {
        return readLock(() -> {
            Pair<TreeSet<PluginSellItem>, TreeSet<PluginUnsoldItem>> pair = indexed.itemsByOwner.get(uuid);
            if (pair == null) return 0;
            return pair.getKey().size();
        });
    }

    public int getUnsoldItemsCountByUser(@NotNull UUID uuid) {
        return readLock(() -> {
            Pair<TreeSet<PluginSellItem>, TreeSet<PluginUnsoldItem>> pair = indexed.itemsByOwner.get(uuid);
            if (pair == null) return 0;
            return pair.getRight().size();
        });
    }


    private class Indexed {
        private final Map<Long, PluginSellItem> sellItemsMap = new HashMap<>();
        private final Map<Long, PluginUnsoldItem> unsoldItemsMap = new HashMap<>();
        private final Map<UUID, Pair<TreeSet<PluginSellItem>, TreeSet<PluginUnsoldItem>>> itemsByOwner = new HashMap<>();
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

        private void addUnsoldItem(@NotNull PluginUnsoldItem unsoldItem) {
            isWriteLock();
            unsoldItemsMap.put(unsoldItem.getId(), unsoldItem);
            Pair<TreeSet<PluginSellItem>, TreeSet<PluginUnsoldItem>> pair = itemsByOwner.computeIfAbsent(unsoldItem.getSellerUuid(), k ->
                    new Pair<>(new TreeSet<>(SELL_ITEM_COMPARATOR), new TreeSet<>(UNSOLD_ITEM_COMPARATOR))
            );
            pair.getRight().add(unsoldItem);
        }

        private void removeUnsoldItem(@NotNull PluginUnsoldItem unsoldItem) {
            isWriteLock();
            unsoldItemsMap.remove(unsoldItem.getId());
            Pair<TreeSet<PluginSellItem>, TreeSet<PluginUnsoldItem>> pair = itemsByOwner.computeIfAbsent(unsoldItem.getSellerUuid(), k ->
                    new Pair<>(new TreeSet<>(SELL_ITEM_COMPARATOR), new TreeSet<>(UNSOLD_ITEM_COMPARATOR))
            );
            pair.getRight().remove(unsoldItem);
            if (pair.getLeft().isEmpty() && pair.getRight().isEmpty()) {
                itemsByOwner.remove(unsoldItem.getSellerUuid());
            }
        }

        private void removeSellItem(@NotNull PluginSellItem sellItem) {
            isWriteLock();
            sellItemsMap.remove(sellItem.getId());
            Pair<TreeSet<PluginSellItem>, TreeSet<PluginUnsoldItem>> pair = itemsByOwner.computeIfAbsent(sellItem.getSellerUuid(), k ->
                    new Pair<>(new TreeSet<>(SELL_ITEM_COMPARATOR), new TreeSet<>(UNSOLD_ITEM_COMPARATOR))
            );
            pair.getLeft().remove(sellItem);
            if (pair.getLeft().isEmpty() && pair.getRight().isEmpty()) {
                itemsByOwner.remove(sellItem.getSellerUuid());
            }
            sortedItems.forEach((key, value) -> {
                Category category = categoryMap.get(key);
                if (category == null) throw new IllegalStateException("unknown category: " + key);
                if (category.matches(sellItem)) {
                    value.values().forEach(sort -> sort.remove(sellItem));
                }
            });
        }

        private void addSellItem(@NotNull PluginSellItem sellItem) {
            isWriteLock();
            sellItemsMap.put(sellItem.getId(), sellItem);
            Pair<TreeSet<PluginSellItem>, TreeSet<PluginUnsoldItem>> pair = itemsByOwner.computeIfAbsent(sellItem.getSellerUuid(), k ->
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
