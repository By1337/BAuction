package org.by1337.bauction.db.kernel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.chat.util.Message;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.User;
import org.by1337.bauction.db.*;
import org.by1337.bauction.db.event.*;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class JsonDBCore implements DBCore {

    private Map<UUID, CSellItem> sellItemsMap = new HashMap<>();
    private Map<UUID, CUnsoldItem> unsoldItemsMap = new HashMap<>();
    private Map<UUID, List<CSellItem>> sellItemsByOwner = new HashMap<>();
    private Map<UUID, List<CUnsoldItem>> unsoldItemsByOwner = new HashMap<>();
    private final Comparator<CSellItem> sellItemComparator = Comparator.comparingLong(i -> i.removalDate);
    private ArrayList<CSellItem> sortedSellItems = new ArrayList<>();
    private final Comparator<CUnsoldItem> unsoldItemComparator = Comparator.comparingLong(i -> i.deleteVia);
    private ArrayList<CUnsoldItem> sortedUnsoldItems = new ArrayList<>();
    private Map<UUID, CUser> users = new HashMap<>();
    private final StorageMap<NameKey, List<SortingItems>> sortedItems = new StorageMap<>();


    private final Gson gson = new Gson();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private final long removeTime;
    private final boolean removeExpiredItems;

    private final Map<NameKey, Category> categoryMap;
    private final Map<NameKey, Sorting> sortingMap;

    private Runnable runnable;
    private Runnable runnable1;


    public JsonDBCore(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap) {
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

        load();
        runnable = () -> {
            long time = System.currentTimeMillis();
            try {
                Long sleep = readLock(() -> {
                    int removed = 0;
                    for (CSellItem sellItem : sortedSellItems) {
                        if (sellItem.removalDate < time) {
                            new Thread(() -> { // new Thread иначе deadlock
                                writeLock(() -> {
                                    expiredItem(sellItem);
                                    return null;
                                });
                            }).start();
                            removed++;
                            if (removed > 20)
                                return 50L * 5;
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

        if (removeExpiredItems) {
            runnable1 = () -> {
                long time = System.currentTimeMillis();
                try {
                    Long sleep = readLock(() -> {
                        int removed = 0;
                        for (CUnsoldItem unsoldItem : sortedUnsoldItems) {
                            if (unsoldItem.deleteVia < time) {
                                new Thread(() -> { // new Thread иначе deadlock
                                    writeLock(() -> {
                                        removeUnsoldItem(unsoldItem);
                                        return null;
                                    });
                                }).start();
                                removed++;
                                if (removed > 20)
                                    return 50L * 5;
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

    }

    private void expiredItem(CSellItem item) {
        if (!lock.isWriteLockedByCurrentThread()) {
            throw new IllegalStateException("Current thread does not hold the write lock");
        }
        tryRemoveItem(item.uuid);
        CUnsoldItem unsoldItem = new CUnsoldItem(item.item, item.sellerUuid, item.removalDate, item.removalDate + removeTime);
        addUnsoldItem(unsoldItem);
    }

    public int getItemsSize() {
        return readLock(sortedSellItems::size);
    }

    public List<CSellItem> getAllItems() {
        return readLock(() -> sortedSellItems);
    }

    public CUser getUserOrCreate(Player player) {
        if (!hasUser(player.getUniqueId())) {
            return createNewAndSave(player.getUniqueId(), player.getName());
        }
        return getUser(player.getUniqueId());
    }

    public void validateAndAddItem(SellItemEvent event) {
        try {
            CUser user = getUser(event.getUser().getUuid());

            CSellItem sellItem = event.getSellItem();

            if (Main.getCfg().getMaxSlots() <= (user.getItemForSale().size() - user.getExternalSlots())) {
                event.setValid(false);
                event.setReason(Lang.getMessages("auction_item_limit_reached"));
                return;
            }
            addItem(sellItem, user.getUuid());
            //   user.dealCount++;
            event.setValid(true);
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
        }
    }

    public void validateAndRemoveItem(TakeItemEvent event) {
        CUser user = event.getUser();
        CSellItem sellItem = event.getSellItem();

        if (!user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessages("not_item_owner"));
            return;
        }
        if (!hasSellItem(sellItem.getUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessages("item_already_sold_or_removed"));
            return;
        }

        try {
            tryRemoveItem(sellItem.getUuid());
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
            return;
        }
        event.setValid(true);
    }

    public void validateAndRemoveItem(TakeUnsoldItemEvent event) {
        try {
            CUser user = getUser(event.getUser().getUuid());
            CUnsoldItem unsoldItem = event.getUnsoldItem();

            if (!user.getUuid().equals(unsoldItem.getSellerUuid())) {
                event.setValid(false);
                event.setReason(Lang.getMessages("not_item_owner"));
                return;
            } else if (!readLock(() -> unsoldItemsMap.containsKey(unsoldItem.uuid))) {
                event.setValid(false);
                event.setReason(Lang.getMessages("item_not_found"));
                return;
            }

            tryRemoveUnsoldItem(user.getUuid(), unsoldItem.getUuid());

            event.setValid(true);
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
        }
    }

    public void validateAndRemoveItem(BuyItemEvent event) {
        CUser user = event.getUser();
        CSellItem sellItem = event.getSellItem();

        if (user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessages("item_owner"));
            return;
        }
        if (!hasSellItem(sellItem.getUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessages("item_already_sold_or_removed"));
            return;
        }

        try {
            tryRemoveItem(sellItem.getUuid());
            CUser owner = getUser(sellItem.sellerUuid);
            owner.dealSum += sellItem.price;
            owner.dealCount++;
            user.dealCount++;
            user.dealSum += sellItem.price;
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
            return;
        }
        event.setValid(true);
    }

    public void validateAndRemoveItem(BuyItemCountEvent event) {
        CUser buyer = event.getUser();
        CSellItem sellItem = event.getSellItem();

        if (buyer.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason(Lang.getMessages("item_owner"));
            return;
        }

        try {
            if (!hasSellItem(sellItem.getUuid())) {
                event.setValid(false);
                event.setReason(Lang.getMessages("item_already_sold_or_removed"));
                return;
            }
            CSellItem updated = getSellItem(sellItem.getUuid());

            if (updated.getAmount() < event.getCount()) {
                event.setValid(false);
                event.setReason(Lang.getMessages("quantity_limit_exceeded"));
                return;
            }
            tryRemoveItem(sellItem.getUuid());
            buyer.dealCount++;
            buyer.dealSum += updated.priceForOne * event.getCount();
            CUser owner = getUser(updated.sellerUuid);
            owner.dealCount++;
            owner.dealSum += updated.priceForOne * event.getCount();
            int newCount = updated.getAmount() - event.getCount();
            ItemStack itemStack = updated.getItemStack().clone();
            itemStack.setAmount(newCount);

            if (newCount != 0) {
                CSellItem newItem = CSellItem.builder()
                        .sellerName(updated.getSellerName())
                        .sellerUuid(updated.getSellerUuid())
                        .price(updated.priceForOne * newCount)
                        .saleByThePiece(true)
                        .tags(updated.getTags())
                        .timeListedForSale(updated.getTimeListedForSale())
                        .removalDate(updated.getRemovalDate())
                        .uuid(updated.getUuid())
                        .material(updated.getMaterial())
                        .amount(newCount)
                        .priceForOne(updated.priceForOne)
                        .itemStack(itemStack)
                        .build();

                addItem(newItem, buyer.getUuid());
            }

        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason(Lang.getMessages("error_occurred"));
            return;
        }
        event.setValid(true);
    }

    public List<CSellItem> getItems(NameKey category, NameKey sorting) {
        return readLock(() -> {
            if (!sortedItems.containsKey(category)) {
                throw new IllegalStateException("unknown category: " + category.getName());
            }
            return sortedItems.get(category).stream().filter(sortingItems -> sortingItems.getSorting().nameKey().equals(sorting)).findFirst().orElseThrow(() -> new IllegalStateException("unknown sorting: " + sorting.getName())).getItems();

        });
    }

    private void removeIf(Predicate<CSellItem> filter) {
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

    public CSellItem getSellItem(UUID uuid) {
        return readLock(() -> sellItemsMap.get(uuid));
    }

    public CUnsoldItem getUnsoldItem(UUID uuid) {
        return readLock(() -> unsoldItemsMap.get(uuid));
    }

    public List<CSellItem> getSellItemsByOwner(UUID ownerUuid) {
        return readLock(() -> sellItemsByOwner.getOrDefault(ownerUuid, new ArrayList<>()));
    }

    public List<CUnsoldItem> getUnsoldItemsByOwner(UUID ownerUuid) {
        return readLock(() -> unsoldItemsByOwner.getOrDefault(ownerUuid, new ArrayList<>()));
    }


    private void addSellItem(CSellItem sellItem) {
        if (!lock.isWriteLockedByCurrentThread()) {
            throw new IllegalStateException("Current thread does not hold the write lock");
        }
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

    private void removeSellItem(CSellItem sellItem) {
        if (!lock.isWriteLockedByCurrentThread()) {
            throw new IllegalStateException("Current thread does not hold the write lock");
        }
        sellItemsMap.remove(sellItem.uuid);

        sortedSellItems.remove(sellItem);

        sellItemsByOwner.computeIfAbsent(sellItem.sellerUuid, k -> new ArrayList<>()).remove(sellItem);
        users.get(sellItem.sellerUuid).itemForSale.remove(sellItem.uuid);
        removeIf(i -> i.uuid.equals(sellItem.getUuid()));
    }

    private void addUnsoldItem(CUnsoldItem unsoldItem) {
        if (!lock.isWriteLockedByCurrentThread()) {
            throw new IllegalStateException("Current thread does not hold the write lock");
        }

        unsoldItemsMap.put(unsoldItem.uuid, unsoldItem);
        int insertIndex = Collections.binarySearch(sortedUnsoldItems, unsoldItem, unsoldItemComparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        sortedUnsoldItems.add(insertIndex, unsoldItem);

        unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).add(unsoldItem);
        users.get(unsoldItem.sellerUuid).unsoldItems.add(unsoldItem.uuid);
    }

    private void removeUnsoldItem(CUnsoldItem unsoldItem) {
        if (!lock.isWriteLockedByCurrentThread()) {
            throw new IllegalStateException("Current thread does not hold the write lock");
        }
        unsoldItemsMap.remove(unsoldItem.uuid);
        sortedUnsoldItems.remove(unsoldItem);

        unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).remove(unsoldItem);
        CUser user = users.get(unsoldItem.sellerUuid);
        user.unsoldItems.remove(unsoldItem.uuid);
    }

    @Override
    public List<CUnsoldItem> getAddUnsoldItems() {
        return readLock(() -> sortedUnsoldItems.stream().toList());
    }

    @Override
    public List<CSellItem> getAllSellItems() {
        return readLock(() -> sortedSellItems.stream().toList());
    }

    @Override
    public List<CUser> getAllUsers() {
        return readLock(() -> users.values().stream().toList());
    }

    @Override
    public CUser getUser(UUID uuid) {
        return readLock(() -> Main.getCfg().getBoostManager().userUpdate(users.get(uuid)));
    }

    @Override
    public boolean hasUser(UUID uuid) {
        return readLock(() -> users.containsKey(uuid));
    }

    @Override
    public boolean hasSellItem(UUID uuid) {
        return readLock(() -> sellItemsMap.containsKey(uuid));
    }

    public CUser createNewAndSave(UUID uuid, String name) {
        return writeLock(() -> {
            CUser user = new CUser(name, uuid);
            users.put(uuid, user);
            return user;
        });
    }


    @Override
    public void addItem(CSellItem memorySellItem, UUID owner) {
        writeLock(() -> {
            CSellItem sellItem = CSellItem.parse(memorySellItem);
            addSellItem(sellItem);
            return null;
        });
    }

    @Override
    public void tryRemoveUnsoldItem(UUID owner, UUID item) {
        writeLock(() -> {
            CUnsoldItem item1 = getUnsoldItem(item);
            if (item1 == null) throw new StorageException.NotFoundException();
            removeUnsoldItem(item1);
            return null;
        });
    }

    @Override
    public void tryRemoveItem(UUID item) {
        writeLock(() -> {
            CSellItem item1 = getSellItem(item);
            if (item1 == null) throw new StorageException.NotFoundException();
            removeSellItem(item1);
            return null;
        });
    }


    @Override
    public void save() {
        try {
            readLock(() -> {
                save0(sortedSellItems, "sellItems", "sell-items-");
                save0(sortedUnsoldItems, "unsoldItems", "unsold-items-");
                save0(users.values().stream().toList(), "users", "users-");
                return null;
            });
        } catch (Exception e) {
            Main.getMessage().error(e);
        }
    }

    @Override
    public void load() {
        try {
            writeLock(() -> {
                TimeCounter timeCounter = new TimeCounter();
                Message message = Main.getMessage();
                message.logger("[DB] load items fom files # step 1");
                List<CSellItem> items = load("sellItems", new TypeToken<List<CSellItem>>() {
                }.getType());

                List<CUser> users = load("users", new TypeToken<List<CUser>>() {
                }.getType());

                List<CUnsoldItem> unsoldItems = load("unsoldItems", new TypeToken<List<CUnsoldItem>>() {
                }.getType());

                message.logger("[DB] # step 1 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] init users # step 2");
                users.forEach(user -> this.users.put(user.getUuid(), user));


                message.logger("[DB] # step 2 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] validate items and unsold items # step 3");
                List<CUnsoldItem> toAdd = new ArrayList<>();
                long time = System.currentTimeMillis();
                {
                    items.removeIf(item -> {

                        if (item.removalDate <= time) {
                            CUnsoldItem unsoldItem = new CUnsoldItem(item.item, item.sellerUuid, item.removalDate, item.removalDate + removeTime);
                            toAdd.add(unsoldItem);

                            CUser user = this.users.get(item.sellerUuid);
                            user.itemForSale.remove(item.uuid);
                            user.unsoldItems.add(unsoldItem.uuid);
                            return true;
                        }
                        return false;
                    });
                }
                {
                    unsoldItems.removeIf(item -> {
                        if (item.deleteVia <= removeTime) {
                            CUser user = this.users.get(item.sellerUuid);
                            user.unsoldItems.remove(item.uuid);
                            return true;
                        }
                        return false;
                    });
                }
                unsoldItems.addAll(toAdd);

                message.logger("[DB] # step 3 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] validate users # step 4");
                { // validate 1

                    items.forEach(item -> {
                        CUser user = this.users.get(item.sellerUuid);
                        if (!user.itemForSale.contains(item.uuid)) {
                            Main.getMessage().error("user %s has no item %s!", user.uuid, item.uuid);
                            user.itemForSale.add(item.uuid);
                        }
                    });

                    unsoldItems.forEach(item -> {
                        CUser user = this.users.get(item.sellerUuid);
                        if (!user.unsoldItems.contains(item.uuid)) {
                            Main.getMessage().error("user %s has no unsold item %s!", user.uuid, item.uuid);
                            user.unsoldItems.add(item.uuid);
                        }
                    });
                }

                message.logger("[DB] # step 4 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] final initialization # step 5");
                items.forEach(item -> {
                    sellItemsMap.put(item.uuid, item);
                    sortedSellItems.add(item);
                    sellItemsByOwner.computeIfAbsent(item.sellerUuid, k -> new ArrayList<>()).add(item);

                    categoryMap.values().forEach(value -> {
                        if (TagUtil.matchesCategory(value, item)) {
                            sortedItems.get(value.nameKey()).forEach(list -> list.addItem(item));
                        }
                    });
                });
                sortedSellItems.sort(sellItemComparator);
                unsoldItems.forEach(unsoldItem -> {
                    unsoldItemsMap.put(unsoldItem.uuid, unsoldItem);
                    sortedUnsoldItems.add(unsoldItem);
                    unsoldItemsByOwner.computeIfAbsent(unsoldItem.sellerUuid, k -> new ArrayList<>()).add(unsoldItem);
                });
                message.logger("[DB] # step 5 completed in %s ms.", timeCounter.getTime());
                timeCounter.reset();

                message.logger("[DB] validate users 2 # step 6");
                this.users.values().forEach(user -> {
                    user.unsoldItems.removeIf(uuid -> {
                        if (!unsoldItemsMap.containsKey(uuid)) {
                            message.error("user %s has non-existent item %s", user.uuid, uuid);
                            return true;
                        }
                        return false;
                    });

                    user.itemForSale.removeIf(uuid -> {
                        if (!sellItemsMap.containsKey(uuid)) {
                            message.error("user %s has non-existent item %s", user.uuid, uuid);
                            return true;
                        }
                        return false;
                    });
                });

                message.logger("[DB] # step 6 completed in %s ms.", timeCounter.getTime());

                message.logger("[DB] total time %s ms.", timeCounter.getTotalTime());
                return null;
            });
        } catch (Exception e) {
            Main.getMessage().error(e);
        }
    }

    private <T> List<T> load(String dir, Type type) {
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
