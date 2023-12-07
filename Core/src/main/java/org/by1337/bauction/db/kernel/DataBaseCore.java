package org.by1337.bauction.db.kernel;

import org.bukkit.entity.Player;
import org.by1337.api.chat.util.Message;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.auc.UnsoldItem;
import org.by1337.bauction.auc.User;
import org.by1337.bauction.db.SortingItems;
import org.by1337.bauction.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.function.Supplier;

@ThreadSafe
public abstract class DataBaseCore {

    private final Map<UUID, CUser> users = new HashMap<>(); // main
    private final ArrayList<CSellItem> sortedSellItems = new ArrayList<>(); // main
    private final ArrayList<CUnsoldItem> sortedUnsoldItems = new ArrayList<>(); // main

    private final Map<UniqueName, CSellItem> sellItemsMap = new HashMap<>();
    private final Map<UniqueName, CUnsoldItem> unsoldItemsMap = new HashMap<>();
    private final Map<UUID, Pair<HashSet<CSellItem>, HashSet<CUnsoldItem>>> itemsByOwner = new HashMap<>();

    private final Comparator<CSellItem> sellItemComparator = Comparator.comparingLong(i -> i.removalDate);
    private final Comparator<CUnsoldItem> unsoldItemComparator = Comparator.comparingLong(i -> i.deleteVia);

    private final Map<NameKey, Map<NameKey, SortingItems>> sortedItems = new HashMap<>();

    protected final Map<NameKey, Category> categoryMap;
    protected final Map<NameKey, Sorting> sortingMap;

    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    protected DataBaseCore(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap) {
        this.categoryMap = categoryMap;
        this.sortingMap = sortingMap;

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

    protected void boostCheck(UUID uuid) {
        if (hasUser(uuid)) {
            writeLock(() -> {
                Main.getCfg().getBoostManager().userUpdate((CUser) getUser(uuid));
            });
        }
    }

    protected void addSellItem0(CSellItem sellItem) {
        isWriteLock();
        if (hasSellItem(sellItem.uniqueName)) {
            throw new IllegalStateException("item already exist");
        }
        sellItemsMap.put(sellItem.uniqueName, sellItem);
        int insertIndex = Collections.binarySearch(sortedSellItems, sellItem, sellItemComparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        sortedSellItems.add(insertIndex, sellItem);

        Pair<HashSet<CSellItem>, HashSet<CUnsoldItem>> pair = getValue(itemsByOwner, sellItem.sellerUuid, () -> new Pair<>(new HashSet<>(), new HashSet<>()));
        pair.getLeft().add(sellItem);
        itemsByOwner.put(sellItem.sellerUuid, pair);

        sortedItems.forEach((key, value) -> {
            Category category = categoryMap.get(key);
            if (category == null) throw new IllegalStateException("unknown category: " + key);
            if (category.matches(sellItem)) {
                value.values().forEach(sort -> sort.addItem(sellItem));
            }
        });
    }

    protected void removeSellItem0(CSellItem sellItem) {
        isWriteLock();
        if (!hasSellItem(sellItem.uniqueName)) {
            throw new IllegalStateException("item non-exist");
        }
        sellItemsMap.remove(sellItem.uniqueName);
        sortedSellItems.remove(sellItem);
        Pair<HashSet<CSellItem>, HashSet<CUnsoldItem>> pair = getValue(itemsByOwner, sellItem.sellerUuid, () -> new Pair<>(new HashSet<>(), new HashSet<>()));
        pair.getLeft().remove(sellItem);
        itemsByOwner.put(sellItem.sellerUuid, pair);
        removeIf(i -> i.getUniqueName().equals(sellItem.getUniqueName()));
    }

    protected void addUnsoldItem0(CUnsoldItem unsoldItem) {
        isWriteLock();
        if (hasUnsoldItem(unsoldItem.uniqueName)) {
            throw new IllegalStateException("item already exist");
        }
        unsoldItemsMap.put(unsoldItem.uniqueName, unsoldItem);
        int insertIndex = Collections.binarySearch(sortedUnsoldItems, unsoldItem, unsoldItemComparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        sortedUnsoldItems.add(insertIndex, unsoldItem);

        Pair<HashSet<CSellItem>, HashSet<CUnsoldItem>> pair = getValue(itemsByOwner, unsoldItem.sellerUuid, () -> new Pair<>(new HashSet<>(), new HashSet<>()));
        pair.getRight().add(unsoldItem);
        itemsByOwner.put(unsoldItem.sellerUuid, pair);
    }

    protected void removeUnsoldItem0(CUnsoldItem unsoldItem) {
        isWriteLock();
        if (hasUnsoldItem(unsoldItem.uniqueName)) {
            throw new IllegalStateException("item non-exist");
        }
        unsoldItemsMap.remove(unsoldItem.uniqueName);
        sortedUnsoldItems.remove(unsoldItem);

        Pair<HashSet<CSellItem>, HashSet<CUnsoldItem>> pair = getValue(itemsByOwner, unsoldItem.sellerUuid, () -> new Pair<>(new HashSet<>(), new HashSet<>()));
        pair.getRight().remove(unsoldItem);
        itemsByOwner.put(unsoldItem.sellerUuid, pair);
    }

    protected CUser addUser(CUser user) {
        isWriteLock();
        if (hasUser(user.uuid)) {
            throw new IllegalStateException("user already exist!");
        }
        users.put(user.uuid, user);
        return user;
    }

    protected void replaceUser(CUser user) {
        isWriteLock();
        if (!hasUser(user.uuid)) {
            throw new IllegalStateException("user non-exist!");
        }
        users.replace(user.uuid, user);
    }

    public void addSellItem(@NotNull SellItem sellItem) {
        writeLock(() -> {
            if (hasSellItem(sellItem.getUniqueName())) {
                throw new IllegalStateException("sell item already exist!");
            }
            if (sellItem instanceof CSellItem cSellItem) {
                addSellItem0(cSellItem);
            } else {
                CSellItem sellItem1 = CSellItem.parse(sellItem);
                addSellItem0(sellItem1);
            }
        });
    }

    public UnsoldItem removeUnsoldItem(UniqueName name) {
        return writeLock(() -> {
            CUnsoldItem item1 = (CUnsoldItem) getUnsoldItem(name);
            if (item1 == null) throw new IllegalStateException("unsold " + name + " non-exist");
            removeUnsoldItem0(item1);
            return item1;
        });
    }

    public void addUnsoldItem(CUnsoldItem unsoldItem) {
        writeLock(() -> {
            if (hasUnsoldItem(unsoldItem.getUniqueName())) {
                throw new IllegalStateException("unsold item " + unsoldItem.getUniqueName() + " already exist!");
            }
            addUnsoldItem0(unsoldItem);
        });
    }


    public SellItem removeSellItem(UniqueName name) {
        return writeLock(() -> {
            CSellItem item1 = (CSellItem) getSellItem(name);
            if (item1 == null) throw new IllegalStateException("sell item " + name + " non-exist");
            removeSellItem0(item1);
            return item1;
        });
    }


    private void removeIf(Predicate<SellItem> filter) {
        isWriteLock();
        sortedItems.values().forEach(map -> map.values().forEach(sortingItems -> sortingItems.removeIf(filter)));
    }

    @Nullable
    public User getUser(UUID uuid) {
        return readLock(() -> users.get(uuid));
    }

    @NotNull
    public Collection<? extends User> getAllUsers() {
        return readLock(users::values);
    }

    @NotNull
    public Collection<? extends UnsoldItem> getAllUnsoldItems() {
        return readLock(() -> sortedUnsoldItems);
    }

    @Nullable
    public UnsoldItem getUnsoldItem(UniqueName name) {
        return readLock(() -> unsoldItemsMap.get(name));
    }

    public boolean hasUnsoldItem(UniqueName name) {
        return readLock(() -> unsoldItemsMap.containsKey(name));
    }

    public boolean hasSellItem(UniqueName name) {
        return readLock(() -> sellItemsMap.containsKey(name));
    }

    @Nullable
    public SellItem getSellItem(UniqueName name) {
        return readLock(() -> sellItemsMap.get(name));
    }

    @NotNull
    public Collection<? extends SellItem> getSellItemsBy(NameKey category, NameKey sorting) {
        return readLock(() -> {
            Map<NameKey, SortingItems> map = sortedItems.get(category);
            if (map == null) {
                throw new IllegalStateException("unknown category: " + category.getName());
            }
            SortingItems sortingItems = map.get(sorting);
            if (sortingItems == null) {
                throw new IllegalStateException("unknown sorting: " + sorting.getName());
            }
            return sortingItems.getItems();
        });
    }

    public boolean hasUser(UUID uuid) {
        return readLock(() -> users.containsKey(uuid));
    }

    public int getSellItemsCount() {
        return readLock(sortedSellItems::size);
    }

    @NotNull
    public Collection<? extends SellItem> getAllSellItems() {
        return readLock(() -> sortedSellItems);
    }

    @NotNull
    public Collection<? extends SellItem> getSellItemsByUser(@NotNull UUID uuid) {
        return readLock(() -> {
            Pair<HashSet<CSellItem>, HashSet<CUnsoldItem>> pair = getValue(itemsByOwner, uuid, () -> new SupplerPair<>(HashSet::new, HashSet::new));
            return pair.getKey();
        });
    }

    @NotNull
    public Collection<? extends UnsoldItem> getUnsoldItemsByUser(@NotNull UUID uuid) {
        return readLock(() -> {
            Pair<HashSet<CSellItem>, HashSet<CUnsoldItem>> pair = getValue(itemsByOwner, uuid, () -> new SupplerPair<>(HashSet::new, HashSet::new));
            return pair.getRight();
        });
    }

    @NotNull
    public User getUserOrCreate(String nickName, UUID uuid) {
        if (hasUser(uuid)) {
            return getUser(uuid);
        } else {
            return writeLock(() -> addUser(new CUser(nickName, uuid)));
        }
    }

    @NotNull
    public User getUserOrCreate(Player player) {
        return getUserOrCreate(player.getName(), player.getUniqueId());
    }

    protected abstract void expiredItem(CSellItem sellItem);

    protected void load(List<CSellItem> items, List<CUser> users, List<CUnsoldItem> unsoldItems) {
        writeLock(() -> {
            Message message = Main.getMessage();
           // long time = System.currentTimeMillis();
            users.removeIf(user -> {
                if (user == null || !user.isValid()) {
                    message.error("cannot be load user %s", String.valueOf(user));
                    return true;
                }
                return false;
            });
            items.removeIf(item -> {
                if (item == null || !item.isValid()) {
                    message.error("cannot be load item %s", String.valueOf(item));
                    return true;
                }
                return false;
            });
            unsoldItems.removeIf(item -> {
                if (item == null || !item.isValid()) {
                    message.error("cannot be load item %s", String.valueOf(item));
                    return true;
                }
                return false;//item.deleteVia <= time;
            });
            users.forEach(user -> this.users.put(user.uuid, user));

            items.forEach(sellItem -> {
                sellItemsMap.put(sellItem.uniqueName, sellItem);
                sortedSellItems.add(sellItem);
                Pair<HashSet<CSellItem>, HashSet<CUnsoldItem>> pair = getValue(itemsByOwner, sellItem.sellerUuid, () -> new Pair<>(new HashSet<>(), new HashSet<>()));
                pair.getLeft().add(sellItem);
                itemsByOwner.put(sellItem.sellerUuid, pair);
                sortedItems.forEach((key, value) -> {
                    Category category = categoryMap.get(key);
                    if (category == null) throw new IllegalStateException("unknown category: " + key);
                    if (category.matches(sellItem)) {
                        value.values().forEach(sort -> sort.addItem(sellItem));
                    }
                });
            });
            sortedSellItems.sort(sellItemComparator);
            unsoldItems.forEach(unsoldItem -> {
                unsoldItemsMap.put(unsoldItem.uniqueName, unsoldItem);
                sortedUnsoldItems.add(unsoldItem);

                Pair<HashSet<CSellItem>, HashSet<CUnsoldItem>> pair = getValue(itemsByOwner, unsoldItem.sellerUuid, () -> new Pair<>(new HashSet<>(), new HashSet<>()));
                pair.getRight().add(unsoldItem);
                itemsByOwner.put(unsoldItem.sellerUuid, pair);
            });
            sortedUnsoldItems.sort(unsoldItemComparator);

//            items.forEach(item -> {
//                if (item.removalDate <= time) {
//                    expiredItem(item);
//                }
//            });
        });
    }

    public abstract void close();


    protected <K, V> V getValue(Map<K, V> map, @NotNull K key, Supplier<V> supplier) {
        V val = map.get(key);
        if (val == null) {
            return supplier.get();
        }
        return val;
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
