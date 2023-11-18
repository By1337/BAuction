package org.by1337.bauction.db.kernel;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.*;
import org.by1337.bauction.db.event.*;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.Sorting;
import org.by1337.bauction.util.TagUtil;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

@ThreadSafe
public class JsonDBCoreV2 implements DBCore {

    //  private final DbActionListener listener0;

    private Map<UUID, SellItem> sellItemsMap = new HashMap<>();
    private Map<UUID, UnsoldItem> unsoldItemsMap = new HashMap<>();
    private Map<UUID, List<SellItem>> sellItemsByOwner = new HashMap<>();
    private Map<UUID, List<UnsoldItem>> unsoldItemsByOwner = new HashMap<>();
    private final Comparator<SellItem> sellItemComparator = Comparator.comparingLong(i -> i.removalDate);
    private ArrayList<SellItem> sortedSellItems = new ArrayList<>();
    private final Comparator<UnsoldItem> unsoldItemComparator = Comparator.comparingLong(i -> i.deleteVia);
    private ArrayList<UnsoldItem> sortedUnsoldItems = new ArrayList<>();
    private Map<UUID, User> users = new HashMap<>();

    private final StorageMap<NameKey, List<SortingItems>> sortedItems = new StorageMap<>();


    private final Gson gson = new Gson();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final long removeTime;
    private final boolean removeExpiredItems;

    private final Map<NameKey, Category> categoryMap;
    private final Map<NameKey, Sorting> sortingMap;

    private Runnable runnable;
    private Runnable runnable1;


    public JsonDBCoreV2(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap) {
        //   this.listener0 = listener;
        this.categoryMap = categoryMap;
        this.sortingMap = sortingMap;
        removeExpiredItems = Main.getCfg().getConfig().getAsBoolean("remove-expired-items.enable");
        removeTime = NumberUtil.getTime(Main.getCfg().getConfig().getAsString("remove-expired-items.time"));

        for (Category category : categoryMap.values()) {
            List<SortingItems> list = new ArrayList<>();
            for (Sorting sorting : sortingMap.values()) {
                list.add(new SortingItems(sorting));
            }
            sortedItems.put(category.nameKey(), list);
        }

        runnable = () -> {
            long time = System.currentTimeMillis();
            try {
                Long sleep = readLock(() -> {
                    for (SellItem sellItem : sortedSellItems) {
                        if (sellItem.removalDate < time) {
                            new Thread(() -> { // new Thread иначе deadlock
                                expiredItem(sellItem);
                            }).start();
                        } else {
                            return Math.min(sellItem.removalDate - time, 50L * 100); // 100 ticks
                        }
                    }
                    return 50L * 100; // 100 ticks
                });
                Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), runnable, sleep / 50);
            } catch (Exception e) {
                Main.getMessage().error(e);
            }
        };
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), runnable, 0);

        runnable1 = () -> {
            long time = System.currentTimeMillis();
            try {
                Long sleep = readLock(() -> {
                    for (UnsoldItem unsoldItem : sortedUnsoldItems) {
                        if (unsoldItem.deleteVia < time) {
                            new Thread(() -> { // new Thread иначе deadlock
                                writeLock(() -> {
                                    removeUnsoldItem(unsoldItem);
                                    return null;
                                });
                            }).start();
                        } else {
                            return Math.min(unsoldItem.deleteVia - time, 50L * 100); // 100 ticks
                        }
                    }
                    return 50L * 100; // 100 ticks
                });
                Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), runnable1, sleep / 50);
            } catch (Exception e) {
                Main.getMessage().error(e);
            }
        };
        Bukkit.getScheduler().runTaskLaterAsynchronously(Main.getInstance(), runnable1, 0);
    }

    private void expiredItem(SellItem item) {
        tryRemoveItem(item.uuid);
        UnsoldItem unsoldItem = new UnsoldItem(item.item, item.sellerUuid, item.removalDate, System.currentTimeMillis() + 1000 * 10);
        addUnsoldItem(unsoldItem);
    }

    public int getItemsSize() {
        return readLock(sortedSellItems::size);
    }

    @ThreadSafe
    public List<SellItem> getAllItems() {
        return readLock(() -> sortedSellItems);
    }

    @ThreadSafe
    public User getUserOrCreate(Player player) {
        if (!hasUser(player.getUniqueId())) {
            return createNewAndSave(player.getUniqueId(), player.getName());
        }
        return getUser(player.getUniqueId());
    }

    public void validateAndAddItem(SellItemEvent event) {
        try {
            User user = getUser(event.getUser().getUuid());
            Main.getCfg().getBoostManager().userUpdate(user);

            SellItem sellItem = event.getSellItem();

            if (Main.getCfg().getMaxSlots() <= (user.getItemForSale().size() - user.getExternalSlots())) {
                event.setValid(false);
                event.setReason("&cВы достигли лимита по количеству предметов на аукционе!");
                return;
            }
            addItem(sellItem, user.getUuid());
            event.setValid(true);
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
        }
    }

    public void addItem(SellItem sellItem, Player player) {
        writeLock(() -> {
            if (!hasUser(player.getUniqueId())) {
                createNewAndSave(player.getUniqueId(), player.getName());
            }
            addItem(sellItem, player.getUniqueId());
            return null;
        });
    }

    public void validateAndRemoveItem(TakeItemEvent event) {
        User user = event.getUser();
        SellItem sellItem = event.getSellItem();

        if (!user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("&cВы не владелец предмета!");
            return;
        }
        if (!hasSellItem(sellItem.getUuid())) {
            event.setValid(false);
            event.setReason("&cПредмет уже продан или снят с продажи!");
            return;
        }

        try {
            tryRemoveItem(sellItem.getUuid());
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            return;
        }
        event.setValid(true);
    }

    public void validateAndRemoveItem(TakeUnsoldItemEvent event) {
        try {
            User user = getUser(event.getUser().getUuid());
            UnsoldItem unsoldItem = event.getUnsoldItem();

            if (!user.getUuid().equals(unsoldItem.getOwner())) {
                event.setValid(false);
                event.setReason("&cВы не владелец предмета!");
                return;
            } else if (!readLock(() -> unsoldItemsMap.containsKey(unsoldItem.uuid))) {
                event.setValid(false);
                event.setReason("&cКажется такого предмета нет!");
                return;
            }

            tryRemoveUnsoldItem(user.getUuid(), unsoldItem.getUuid());

            event.setValid(true);
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
        }
    }

    public void validateAndRemoveItem(BuyItemEvent event) {
        User user = event.getUser();
        SellItem sellItem = event.getSellItem();

        if (user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("&cВы владелец предмета!");
            return;
        }
        if (!hasSellItem(sellItem.getUuid())) {
            event.setValid(false);
            event.setReason("&cПредмет уже продан или снят с продажи!");
            return;
        }

        try {
            tryRemoveItem(sellItem.getUuid());
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            return;
        }
        event.setValid(true);
    }

    public void validateAndRemoveItem(BuyItemCountEvent event) {
        User buyer = event.getUser();
        SellItem sellItem = event.getSellItem();

        if (buyer.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("&cВы владелец предмета!");
            return;
        }

        try {
            if (!hasSellItem(sellItem.getUuid())) {
                event.setValid(false);
                event.setReason("&cПредмет уже продан или снят с продажи!");
                return;
            }
            SellItem updated = getSellItem(sellItem.getUuid());

            if (updated.getAmount() < event.getCount()) {
                event.setValid(false);
                event.setReason("&cКто-то выкупил часть товара и вы больше не можете купить этот предмет в таком количестве!");
                return;
            }
            tryRemoveItem(sellItem.getUuid());

            int newCount = updated.getAmount() - event.getCount();

            if (newCount != 0) {
                SellItem newItem = SellItem.builder()
                        .sellerName(updated.getSellerName())
                        .sellerUuid(updated.getSellerUuid())
                        .price(updated.getPrice())
                        .saleByThePiece(true)
                        .tags(updated.getTags())
                        .timeListedForSale(updated.getTimeListedForSale())
                        .removalDate(updated.getRemovalDate())
                        .uuid(updated.getUuid())
                        .material(updated.getMaterial())
                        .amount(newCount)
                        .priceForOne(updated.getPrice() / newCount)
                        //.sellFor(updated.getSellFor())
                        .itemStack(updated.getItemStack())
                        .build();

                addItem(newItem, buyer.getUuid());
            }

        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            return;
        }
        event.setValid(true);
    }

    @ThreadSafe
    public List<SellItem> getItems(NameKey category, NameKey sorting) {
        return readLock(() -> {
            if (!sortedItems.containsKey(category)) {
                throw new IllegalStateException("unknown category: " + category.getName());
            }
            return sortedItems.get(category).stream().filter(sortingItems -> sortingItems.getSorting().nameKey().equals(sorting)).findFirst().orElseThrow(() -> new IllegalStateException("unknown sorting: " + sorting.getName())).getItems();

        });
    }

    @ThreadSafe
    private void removeIf(Predicate<SellItem> filter) {
        writeLock(() -> {
            Iterator<List<SortingItems>> iterator = sortedItems.values().iterator();
            while (iterator.hasNext()) {
                List<SortingItems> list = iterator.next();
                Iterator<SortingItems> iterator1 = list.listIterator();
                while (iterator1.hasNext()) {
                    SortingItems sortingItems = iterator1.next();
                    sortingItems.removeIf(filter);
                }
            }
            return null;
        });
    }

    @ThreadSafe
    public SellItem getSellItem(UUID uuid) {
        return readLock(() -> sellItemsMap.get(uuid));
    }

    @ThreadSafe
    public UnsoldItem getUnsoldItem(UUID uuid) {
        return readLock(() -> unsoldItemsMap.get(uuid));
    }

    @ThreadSafe
    public List<SellItem> getSellItemsByOwner(UUID ownerUuid) {
        return readLock(() -> sellItemsByOwner.getOrDefault(ownerUuid, new ArrayList<>()));
    }

    @ThreadSafe
    public List<UnsoldItem> getUnsoldItemsByOwner(UUID ownerUuid) {
        return readLock(() -> unsoldItemsByOwner.getOrDefault(ownerUuid, new ArrayList<>()));
    }


    private void addSellItem(SellItem sellItem) {
        sellItemsMap.put(sellItem.uuid, sellItem);
        int insertIndex = Collections.binarySearch(sortedSellItems, sellItem, sellItemComparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        sortedSellItems.add(insertIndex, sellItem);

        sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).add(sellItem);
        users.get(sellItem.sellerUuid).itemForSale.add(sellItem.uuid);
        for (Category value : categoryMap.values()) {
            if (TagUtil.matchesCategory(value, sellItem)) {
                sortedItems.get(value.nameKey()).forEach(list -> list.addItem(sellItem));
            }
        }
    }

    private void removeSellItem(SellItem sellItem) {
        sellItemsMap.remove(sellItem.uuid);

        sortedSellItems.remove(sellItem);

        sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).remove(sellItem);
        users.get(sellItem.sellerUuid).itemForSale.remove(sellItem.uuid);
        removeIf(i -> i.uuid.equals(sellItem.getUuid()));
    }

    private void addUnsoldItem(UnsoldItem unsoldItem) {
        unsoldItemsMap.put(unsoldItem.uuid, unsoldItem);
        int insertIndex = Collections.binarySearch(sortedUnsoldItems, unsoldItem, unsoldItemComparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        sortedUnsoldItems.add(insertIndex, unsoldItem);

        unsoldItemsByOwner.computeIfAbsent(unsoldItem.owner, k -> new ArrayList<>()).add(unsoldItem);
        users.get(unsoldItem.owner).unsoldItems.add(unsoldItem.uuid);
    }

    private void removeUnsoldItem(UnsoldItem unsoldItem) {
        unsoldItemsMap.remove(unsoldItem.uuid);
        sortedUnsoldItems.remove(unsoldItem);

        unsoldItemsByOwner.computeIfAbsent(unsoldItem.owner, k -> new ArrayList<>()).remove(unsoldItem);
        users.get(unsoldItem.owner).unsoldItems.remove(unsoldItem.uuid);
    }

    @ThreadSafe
    @Override
    public List<UnsoldItem> getAddUnsoldItems() {
        return readLock(() -> sortedUnsoldItems.stream().toList());
    }

    @ThreadSafe
    @Override
    public List<SellItem> getAllSellItems() {
        return readLock(() -> sortedSellItems.stream().toList());
    }

    @Override
    @ThreadSafe
    public List<User> getAllUsers() {
        return readLock(() -> users.values().stream().toList());
    }

    @Override
    @ThreadSafe
    public User getUser(UUID uuid) {
        return readLock(() -> users.get(uuid));
    }

    @Override
    @ThreadSafe
    public boolean hasUser(UUID uuid) {
        return readLock(() -> users.containsKey(uuid));
    }

    @Override
    @ThreadSafe
    public boolean hasSellItem(UUID uuid) {
        return readLock(() -> sellItemsMap.containsKey(uuid));
    }

    @ThreadSafe
    public User createNewAndSave(UUID uuid, String name) {
        return writeLock(() -> {
            User user = new User(name, uuid);
            users.put(uuid, user);
            //  listener0.update(new Action(DBActionType.USER_CREATE, uuid, null));
            return user;
        });
    }


    @Override
    @ThreadSafe
    public void addItem(SellItem memorySellItem, UUID owner) {
        writeLock(() -> {
            SellItem sellItem = SellItem.parse(memorySellItem);
            addSellItem(sellItem);

            //    listener0.update(new Action(DBActionType.USER_ADD_SELL_ITEM, owner, sellItem.uuid));
            //   listener0.update(new Action(DBActionType.AUCTION_ADD_SELL_ITEM, null, sellItem.uuid));

            return null;
        });
    }

    @Override
    @ThreadSafe
    public void tryRemoveUnsoldItem(UUID owner, UUID item) {
        writeLock(() -> {
            UnsoldItem item1 = getUnsoldItem(item);
            if (item1 == null) throw new StorageException.NotFoundException();
            removeUnsoldItem(item1);

            //     listener0.update(new Action(DBActionType.USER_REMOVE_UNSOLD_ITEM, owner, item));

            return null;
        });
    }

    @Override
    @ThreadSafe
    public void tryRemoveItem(UUID item) {
        writeLock(() -> {
            SellItem item1 = getSellItem(item);
            if (item1 == null) throw new StorageException.NotFoundException();
            removeSellItem(item1);
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

    private <T> T writeLock(Task<T> task) {
        lock.writeLock().lock();
        T res;
        try {
            res = task.run();
            return res;
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private <T> T readLock(Task<T> task) {
        lock.readLock().lock();
        T res;
        try {
            res = task.run();
            return res;
        } catch (StorageException e) {
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }
    }

}
