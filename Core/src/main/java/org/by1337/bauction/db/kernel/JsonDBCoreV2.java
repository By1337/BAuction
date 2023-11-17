package org.by1337.bauction.db.kernel;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.*;
import org.by1337.bauction.db.action.Action;
import org.by1337.bauction.db.action.DBActionType;
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

@ThreadSafe
public class JsonDBCoreV2 implements DBCore {

    private final DbActionListener listener0;

    private Map<UUID, SellItem> sellItemsMap = new HashMap<>();
    private Map<UUID, UnsoldItem> unsoldItemsMap = new HashMap<>();
    private Map<UUID, List<SellItem>> sellItemsByOwner = new HashMap<>();
    private Map<UUID, List<UnsoldItem>> unsoldItemsByOwner = new HashMap<>();
    private TreeSet<SellItem> sortedSellItems = new TreeSet<>(Comparator.comparingLong(i -> i.removalDate));
    private TreeSet<UnsoldItem> sortedUnsoldItems = new TreeSet<>(Comparator.comparingLong(i -> i.deleteVia));

    private Map<UUID, User> users = new HashMap<>();

    private final Gson gson = new Gson();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final long removeTime;
    private final boolean removeExpiredItems;

    public JsonDBCoreV2(DbActionListener listener) {
        this.listener0 = listener;
        removeExpiredItems = Main.getCfg().getConfig().getAsBoolean("remove-expired-items.enable");
        removeTime = NumberUtil.getTime(Main.getCfg().getConfig().getAsString("remove-expired-items.time"));
    }


    private SellItem getSellItem(UUID uuid) {
        return sellItemsMap.get(uuid);
    }

    private UnsoldItem getUnsoldItem(UUID uuid) {
        return unsoldItemsMap.get(uuid);
    }

    private List<SellItem> getSellItemsByOwner(UUID ownerUuid) {
        return sellItemsByOwner.getOrDefault(ownerUuid, new ArrayList<>());
    }

    private List<UnsoldItem> getUnsoldItemsByOwner(UUID ownerUuid) {
        return unsoldItemsByOwner.getOrDefault(ownerUuid, new ArrayList<>());
    }

    private void addSellItem(SellItem sellItem) {
        sellItemsMap.put(sellItem.uuid, sellItem);
        sortedSellItems.add(sellItem);

        sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).add(sellItem);
        users.get(sellItem.sellerUuid).itemForSale.add(sellItem.uuid);
    }

    private void removeSellItem(SellItem sellItem) {
        sellItemsMap.remove(sellItem.uuid);
        sortedSellItems.remove(sellItem);

        sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).remove(sellItem);
        users.get(sellItem.sellerUuid).itemForSale.remove(sellItem.uuid);
    }

    private void addUnsoldItem(UnsoldItem unsoldItem) {
        unsoldItemsMap.put(unsoldItem.uuid, unsoldItem);
        sortedUnsoldItems.add(unsoldItem);

        unsoldItemsByOwner.computeIfAbsent(unsoldItem.owner, k -> new ArrayList<>()).add(unsoldItem);
        users.get(unsoldItem.owner).unsoldItems.add(unsoldItem.uuid);
    }

    private void removeUnsoldItem(UnsoldItem unsoldItem) {
        unsoldItemsMap.remove(unsoldItem.uuid);
        sortedUnsoldItems.remove(unsoldItem);

        unsoldItemsByOwner.computeIfAbsent(unsoldItem.owner, k -> new ArrayList<>()).remove(unsoldItem);
        users.get(unsoldItem.owner).unsoldItems.remove(unsoldItem.uuid);
    }

    @Override
    public List<MemoryUnsoldItem> getAddUnsoldItems() throws StorageException {
        return readLock(() -> sortedUnsoldItems.stream().map(UnsoldItem::toMemoryUnsoldItem).toList());
    }

    @Override
    public List<MemorySellItem> getAllSellItems() throws StorageException {
        return readLock(() -> sortedSellItems.stream().map(SellItem::toMemorySellItem).toList());
    }

    @Override
    public List<MemoryUser> getAllUsers() throws StorageException {
        return readLock(() -> users.values().stream().map(User::toMemoryUser).toList());
    }

    @Override
    public MemoryUser getUser(UUID uuid) throws StorageException {
        return readLock(() -> users.get(uuid).toMemoryUser());
    }

    @Override
    public boolean hasUser(UUID uuid) throws StorageException {
        return readLock(() -> users.containsKey(uuid));
    }

    @Override
    public boolean hasSellItem(UUID uuid) throws StorageException {
        return readLock(() -> sellItemsMap.containsKey(uuid));
    }

    public MemoryUser createNew(UUID uuid, String name) throws StorageException {
        return writeLock(() -> {
            User user = new User(name, uuid);
            users.put(uuid, user);
            listener0.update(new Action(DBActionType.USER_CREATE, uuid, null));
            return user.toMemoryUser();
        });
    }


    @Override
    public void addItem(MemorySellItem memorySellItem, UUID owner) throws StorageException {
        writeLock(() -> {
            SellItem sellItem = SellItem.parse(memorySellItem);
            addSellItem(sellItem);

            listener0.update(new Action(DBActionType.USER_ADD_SELL_ITEM, owner, sellItem.uuid));
            listener0.update(new Action(DBActionType.AUCTION_ADD_SELL_ITEM, null, sellItem.uuid));

            return null;
        });
    }

    @Override
    public void tryRemoveUnsoldItem(UUID owner, UUID item) throws StorageException {
        writeLock(() -> {
            UnsoldItem item1 = getUnsoldItem(item);
            if (item1 == null) throw new StorageException.ItemNotFoundException();
            removeUnsoldItem(item1);

            listener0.update(new Action(DBActionType.USER_REMOVE_UNSOLD_ITEM, owner, item));

            return null;
        });
    }

    @Override
    public void tryRemoveItem(UUID item) throws StorageException {
        writeLock(() -> {
            SellItem item1 = getSellItem(item);
            if (item1 == null) throw new StorageException.ItemNotFoundException();
            removeSellItem(item1);

            listener0.update(new Action(DBActionType.USER_REMOVE_SELL_ITEM, item1.sellerUuid, item));
            listener0.update(new Action(DBActionType.AUCTION_REMOVE_SELL_ITEM, null, item));

            return null;
        });
    }

    @Override
    public void save() {
//        try {
//            readLock(() -> {
//                save0(sellItems.getList(), "items", "items-");
//                save0(users.values().stream().toList(), "users", "users-");
//                return null;
//            });
//        } catch (Exception e) {
//            Main.getMessage().error(e);
//        }
    }

    @Override
    public void load() {
//        try {
//            writeLock(() -> {
//                List<SellItem> items = load("items", new TypeToken<List<SellItem>>() {
//                }.getType());
//                List<User> users = load("users", new TypeToken<List<User>>() {
//                }.getType());
//                for (User user : users) {
//                    this.users.put(user.uuid, user);
//                }
//                sellItems.push(items);
//                return null;
//            });
//        } catch (Exception e) {
//            Main.getMessage().error(e);
//        }
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

}
