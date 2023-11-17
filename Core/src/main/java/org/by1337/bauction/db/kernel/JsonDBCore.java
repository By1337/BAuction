package org.by1337.bauction.db.kernel;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.*;
import org.by1337.bauction.util.NumberUtil;

import javax.annotation.concurrent.ThreadSafe;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@ThreadSafe
public class JsonDBCore /*implements DBCore*/ {
/*

    private final StorageMap<UUID, User> users = new StorageMap<>();
    private final SellItemList sellItems = new SellItemList();

    protected final Gson gson = new Gson();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private Runnable expiredDetector;

    private boolean removeExpiredItems;
    private long removeTime;

    private final DbActionListener listener;

    public JsonDBCore(DbActionListener listener) {
        this.listener = listener;
        removeExpiredItems = Main.getCfg().getConfig().getAsBoolean("remove-expired-items.enable");
        removeTime = NumberUtil.getTime(Main.getCfg().getConfig().getAsString("remove-expired-items.time"));
        load();
        expiredDetector = () -> {
            long time = System.currentTimeMillis();
            try {
                Long sleep = readLock(() -> {
                    for (SellItem sellItem : sellItems.getList()) {
                        if (sellItem.removalDate <= time) {
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
                Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), expiredDetector, sleep / 50);
            } catch (Exception e) {
                Main.getMessage().error(e);
            }
        };
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), expiredDetector, 0);

//        Bukkit.getScheduler().runTaskTimer(Main.getInstance(), () -> {
//            TimeCounter counter = new TimeCounter();
//            try {
//                readLock(() -> {
//                    int x = 0;
//                    long time = System.currentTimeMillis();
//                    for (Map.Entry<UUID, User> entry : users.entrySet()) {
//                        for (UnsoldItem item : new ArrayList<>(entry.getValue().unsoldItems)) {
//                            if (item.deleteVia <= time){
//                                x++;
//                                new Thread(() -> { // new Thread иначе deadlock
//                                    TimeCounter timeCounter = new TimeCounter();
//                                    try {
//                                      tryRemoveUnsoldItem(entry.getKey(), item.uuid);
//                                    } catch (StorageException e) {
//                                        throw new RuntimeException(e);
//                                    }
//                                    System.out.println(timeCounter.getTime());
//                                }).start();
//                            }
//                         //   if (x > 25) break; // limit
//                        }
//                    }
//                    return null;
//                });
//            } catch (Exception e) {
//                Main.getMessage().error(e);
//            }
//            System.out.println(counter.getTime());
//        }, 100, 100);
    }

    private <T> T writeLock(Task<T> task) throws StorageException {
        lock.writeLock().lock();
        T res;
        try {
            res = task.run();
            return res;
        } finally {
            lock.writeLock().unlock();
        }
    }

    private <T> T readLock(Task<T> task) throws StorageException {
        lock.readLock().lock();
        T res;
        try {
            res = task.run();
            return res;
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<MemorySellItem> getAllSellItems() throws StorageException {
        return readLock(() -> sellItems.getList().stream().map(SellItem::toMemorySellItem).collect(Collectors.toList()));
    }

    public List<MemoryUser> getAllUsers() throws StorageException {
        return readLock(() -> users.values().stream().map(User::toMemoryUser).collect(Collectors.toList()));
    }

    public MemoryUser getUser(UUID uuid) throws StorageException {
        return readLock(() -> users.getOrThrow(uuid, StorageException.ItemNotFoundException::new).toMemoryUser());
    }

    public boolean hasUser(UUID uuid) throws StorageException {
        return readLock(() -> users.containsKey(uuid));
    }

    public boolean hasSellItem(UUID uuid) throws StorageException {
        return readLock(() -> sellItems.containsKey(uuid));
    }


    public MemoryUser createNew(UUID uuid, String name) throws StorageException {
        return writeLock(() -> {
            User user = new User(name, uuid);
            users.put(uuid, user);
            listener.update(new Action<>(ActionType.UPDATE_MEMORY_USER, user.toMemoryUser()));
            return user.toMemoryUser();
        });
    }

    private void expiredItem(SellItem item) throws StorageException {
        tryRemoveItem(item.uuid);
        writeLock(() -> {
            User user = users.getOrThrow(item.sellerUuid, () -> new StorageException.ItemNotFoundException("unknown user: " + item.sellerUuid));
            user.unsoldItems.add(new UnsoldItem(item.item, item.sellerUuid, item.removalDate,
                    removeExpiredItems ? System.currentTimeMillis() + removeTime : System.currentTimeMillis() * 2
            ));
            listener.update(create(ActionType.UPDATE_MEMORY_USER, user.toMemoryUser()));
            return null;
        });

    }

    public void addItem(MemorySellItem memorySellItem, UUID owner) throws StorageException {
        writeLock(() -> {
            User user = users.getOrThrow(owner, () -> new StorageException.ItemNotFoundException("unknown user: " + owner));
            SellItem sellItem = SellItem.parse(memorySellItem);
            user.itemForSale.add(sellItem.uuid);
            sellItems.put(sellItem.uuid, sellItem);
            listener.update(create(ActionType.UPDATE_MEMORY_USER, user.toMemoryUser()));
            listener.update(create(ActionType.UPDATE_MEMORY_SELL_ITEM, sellItem.toMemorySellItem()));

            return null;
        });
    }

    public ActionResult tryRemoveUnsoldItem(UUID owner, UUID item) throws StorageException {
        return writeLock(() -> {
            User user = users.getOrThrow(owner, () -> new StorageException.LostItemOwner(item.toString()));
            if (user.unsoldItems.stream().noneMatch(i -> i.uuid.equals(item))) {
                throw new StorageException.LostItemException(item.toString());
            }
            user.unsoldItems.removeIf(i -> i.uuid.equals(item));

            listener.update(new Action<>(ActionType.UPDATE_MEMORY_USER, user.toMemoryUser()));
            return ActionResult.OK;
        });
    }

    public ActionResult tryRemoveItem(UUID itemUuid) throws StorageException {
        return writeLock(() -> {
            SellItem item = sellItems.getOrThrow(itemUuid, () -> new StorageException.ItemNotFoundException("item: " + itemUuid));
            User user = users.getOrThrow(item.sellerUuid, () -> new StorageException.LostItemOwner(item.toString()));

            if (!user.itemForSale.contains(item.uuid))
                throw new StorageException.LostItemException(String.format("user %s has no item %s!", user.uuid, item.toString()));

            user.itemForSale.remove(item.uuid);

            sellItems.remove(item.uuid);

            MemoryUser memoryUser = user.toMemoryUser();
            listener.update(create(ActionType.REMOVE_SELL_ITEM, itemUuid));
            listener.update(create(ActionType.UPDATE_MEMORY_USER, memoryUser));
            return ActionResult.OK;
        });
    }

    private <T> Action<T> create(ActionType<T> type, T val) {
        return new Action<>(type, val);
    }

    public void save() {
        try {
            readLock(() -> {
                save0(sellItems.getList(), "items", "items-");
                save0(users.values().stream().toList(), "users", "users-");
                return null;
            });
        } catch (Exception e) {
            Main.getMessage().error(e);
        }
    }

    public void load() {
        try {
            writeLock(() -> {
                List<SellItem> items = load("items", new TypeToken<List<SellItem>>() {
                }.getType());
                List<User> users = load("users", new TypeToken<List<User>>() {
                }.getType());
                for (User user : users) {
                    this.users.put(user.uuid, user);
                }
                sellItems.push(items);
                return null;
            });
        } catch (Exception e) {
            Main.getMessage().error(e);
        }
    }

    private <T> List<T> load(String dir, Type type) { //new TypeToken<>().getType()
        File home = new File(Main.getInstance().getDataFolder() + "/" + dir);
        List<T> out = new ArrayList<>();
        try {
            if (!home.exists()) {
                home.mkdir();
            }
            for (File file : home.listFiles()) {
                try (FileReader reader = new FileReader(file)) {
                    out.addAll(gson.fromJson(reader, type));
                }
            }
        } catch (IOException e) {
            Main.getMessage().error("failed to save!", e);
        }
        return out;
    }

    private <T> void save0(List<T> list, String dir, String filePrefix) {
        File home = new File(Main.getInstance().getDataFolder() + "/" + dir);
        try {
            if (!home.exists()) {
                home.mkdir();
            }

            for (File file : home.listFiles()) {
                file.delete();
            }
            int max = 10000;
            int last = 0;

            int total = list.size();
            List<T> buffer = new ArrayList<>();
            for (int i = 0; i < total; i++) {
                buffer.add(list.get(i));
                if (i - last >= max || i == total - 1) {
                    File file = new File(home + "/" + filePrefix + (last + 1) + "-" + (i) + ".json");
                    file.createNewFile();
                    try (FileWriter writer = new FileWriter(file)) {
                        Gson gson = new Gson();
                        gson.toJson(buffer, writer);
                        buffer.clear();
                        last = i;
                    }
                }
            }
        } catch (IOException e) {
            Main.getMessage().error("failed to save!", e);
        }
    }
*/

}
