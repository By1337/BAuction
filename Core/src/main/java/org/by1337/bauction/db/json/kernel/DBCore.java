package org.by1337.bauction.db.json.kernel;

import com.google.common.collect.Comparators;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.MemorySellItem;
import org.by1337.bauction.db.MemoryUser;
import org.by1337.bauction.db.StorageException;
import org.by1337.bauction.db.StorageMap;
import org.by1337.bauction.db.json.Action;
import org.by1337.bauction.db.json.ActionResult;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@ThreadSafe
public abstract class DBCore {

    private final StorageMap<UUID, User> users = new StorageMap<>();
    private final ListMap<UUID, SellItem> sellItems = new ListMap<>(Comparator.comparingLong(i -> i.removalDate));
    protected final Gson gson = new Gson();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private BukkitTask task;
    private Runnable runnable;
    public DBCore() {
        runnable = () -> {
            long time = System.currentTimeMillis();
            try {
                Long sleep = readLock(() -> {
                    for (SellItem sellItem : sellItems.getList()) {
                        if (sellItem.removalDate < time) {
                            new Thread(() -> { // new Thread иначе deadlock
                                try {
                                    expiredItem(sellItem);
                                } catch (StorageException e) {
                                    throw new RuntimeException(e);
                                }
                            }).start();
                        } else {
                            return Math.min(sellItem.removalDate - time, 50L * 100); // 100 ticks
                        }
                    }
                    return 50L * 100; // 100 ticks
                });
                task = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), runnable, sleep / 50);
            } catch (Exception e) {
                Main.getMessage().error(e);
            }
        };
        task = Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), runnable, 0);
    }

    private <T> T writeLock(Task<T> task) throws StorageException {
        lock.writeLock().lock();
        T res = null;
        try {
            res = task.run();
            return res;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private <T> T readLock(Task<T> task) throws StorageException {
        lock.readLock().lock();
        T res = null;
        try {
            res = task.run();
            return res;
        } finally {
            lock.readLock().unlock();
        }
    }

    protected List<MemorySellItem> getAllSellItems() throws StorageException {
        return readLock(() -> sellItems.getList().stream().map(SellItem::toMemorySellItem).collect(Collectors.toList()));
    }

    protected List<MemoryUser> getAllUsers() throws StorageException {
        return readLock(() -> users.values().stream().map(User::toMemoryUser).collect(Collectors.toList()));
    }

    protected MemoryUser getUser(UUID uuid) throws StorageException {
        return readLock(() -> users.getOrThrow(uuid, StorageException.NotFoundException::new).toMemoryUser());
    }

    protected boolean hasUser(UUID uuid) throws StorageException {
        return readLock(() -> users.containsKey(uuid));
    }

    protected boolean hasSellItem(UUID uuid) throws StorageException {
        return readLock(() -> sellItems.containsKey(uuid));
    }


    protected MemoryUser createNew(UUID uuid, String name) throws StorageException {
        return writeLock(() -> {
            User user = new User(name, uuid);
            users.put(uuid, user);
            update(new Action<>(ActionType.UPDATE_MEMORY_USER, user.toMemoryUser()));
            return user.toMemoryUser();
        });
    }

    private void expiredItem(SellItem item) throws StorageException {

        tryRemoveItem(item.uuid);

        writeLock(() -> {
            User user = users.getOrThrow(item.sellerUuid, () -> new StorageException.NotFoundException("unknown user: " + item.sellerUuid));
            user.unsoldItems.add(new UnsoldItem(item.item, item.sellerUuid, item.removalDate, item.removalDate + 99999L));
            Action<?> updateUser = generateUpdateUserLog(user);
            logger(updateUser);
            update(new Action<>(ActionType.UPDATE_MEMORY_USER, user.toMemoryUser()));
            return null;
        });

    }

    protected void addItem(MemorySellItem memorySellItem, UUID owner) throws StorageException {
        writeLock(() -> {
            User user = users.getOrThrow(owner, () -> new StorageException.NotFoundException("unknown user: " + owner));
            SellItem sellItem = SellItem.parse(memorySellItem);
            user.itemForSale.add(sellItem.uuid);
            sellItems.put(sellItem.uuid, sellItem);

            Action<?> updateUser = generateUpdateUserLog(user);
            Action<?> newItemLog = generateUpdateSellItem(sellItem);
            logger(updateUser);
            logger(newItemLog);
            update(new Action<>(ActionType.UPDATE_MEMORY_USER, user.toMemoryUser()));
            update(new Action<>(ActionType.UPDATE_MEMORY_SELL_ITEM, sellItem.toMemorySellItem()));
            return null;
        });
    }


    //    protected ActionResult tryRemoveUnsoldItem(UUID itemUuid, UUID owner) throws StorageException {
//        return writeLock(() -> {
//            User user = users.getOrThrow(owner, () -> new StorageException.NotFoundException("unknown user: " + owner));
//            UnsoldItem unsoldItem = user.unsoldItems.stream().filter(item -> item.uuid.equals(itemUuid)).findFirst().orElseThrow(() -> new StorageException.NotFoundException("missing item: " + itemUuid + " owner: " + owner));
//            user.unsoldItems.remove(unsoldItem);
//            MemoryUser memoryUser = user.toMemoryUser();
//            Action<?> updateUser = generateUpdateUserLog(user);
//            logger(updateUser);
//            update(new Action<>(ActionType.UPDATE_MEMORY_USER, memoryUser));
//
//            return ActionResult.OK;
//        });
//    }
    protected ActionResult tryRemoveUnsoldItem(UUID owner, UUID item) throws StorageException {
        return writeLock(() -> {
            User user = users.getOrThrow(owner, () -> new StorageException.LostItemOwner(item.toString()));
            if (user.unsoldItems.stream().noneMatch(i -> i.uuid.equals(item))) {
                throw new StorageException.LostItemException(item.toString());
            }
            user.unsoldItems.removeIf(i -> i.uuid.equals(item));
            Action<?> updateUser = generateUpdateUserLog(user);
            logger(updateUser);
            update(new Action<>(ActionType.UPDATE_MEMORY_USER, user.toMemoryUser()));
            return ActionResult.OK;
        });
    }

    protected ActionResult tryRemoveItem(UUID itemUuid) throws StorageException {
        return writeLock(() -> {
            SellItem item = sellItems.getOrThrow(itemUuid, () -> new StorageException.NotFoundException("item: " + itemUuid));
            User user = users.getOrThrow(item.sellerUuid, () -> new StorageException.LostItemOwner(item.toString()));

            if (!user.itemForSale.contains(item.uuid))
                throw new StorageException.LostItemException(String.format("user %s has no item %s!", user.uuid, item.toString()));

            user.itemForSale.remove(item.uuid);

            sellItems.remove(item.uuid);

            MemoryUser memoryUser = user.toMemoryUser();
            Action<?> removeItem = generateRemoveItemLog(itemUuid);
            Action<?> updateUser = generateUpdateUserLog(user);
            logger(removeItem);
            logger(updateUser);
            update(removeItem);
            update(new Action<>(ActionType.UPDATE_MEMORY_USER, memoryUser));
            return ActionResult.OK;
        });
    }

    protected abstract void update(Action<?> action);

    private Action<User> generateUpdateUserLog(User user) {
        return new Action<>(ActionType.UPDATE_USER, user);
    }

    private Action<UUID> generateRemoveItemLog(UUID uuid) {
        return new Action<>(ActionType.REMOVE_SELL_ITEM, uuid);
    }

    private Action<SellItem> generateUpdateSellItem(SellItem sellItem) {
        return new Action<>(ActionType.UPDATE_SELL_ITEM, sellItem);
    }


    protected void logger(Action<?> action) {
        Main.getMessage().logger(gson.toJson(action));
    }

    @FunctionalInterface
    protected interface Task<T> {
        T run() throws StorageException;
    }

    @FunctionalInterface
    protected interface PostTask<T> {
        void run(@Nullable T val);
    }

    protected class ListMap<K, V> {
        private final Map<K, V> map = new HashMap<>();
        private final ArrayList<V> list = new ArrayList<>();
        private final Comparator<V> comparator;

        public ListMap(Comparator<V> comparator) {
            this.comparator = comparator;
        }

        public <X extends Throwable> V getOrThrow(K key, Supplier<? extends X> def) throws X {
            V value = map.get(key);
            if (value == null) {
                throw def.get();
            }
            return value;
        }

        public V get(K key) {
            return map.get(key);
        }

        public boolean containsKey(K key) {
            return map.containsKey(key);
        }

        public <T extends V> void put(K key, T value) {
            map.put(key, value);

            int insertIndex = Collections.binarySearch(list, value, comparator);
            if (insertIndex < 0) {
                insertIndex = -insertIndex - 1;
            }
            list.add(insertIndex, value);
        }

        public void remove(K key) {
            V val = map.get(key);
            map.remove(key);
            list.remove(val);
        }

        public Map<K, V> getMap() {
            return map;
        }

        public ArrayList<V> getList() {
            return list;
        }

        public Comparator<V> getComparator() {
            return comparator;
        }
    }
}
