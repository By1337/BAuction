package org.by1337.bauction.db;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.action.Action;
import org.by1337.bauction.db.action.DBActionType;
import org.by1337.bauction.db.event.*;
import org.by1337.bauction.db.kernel.DBCore;
import org.by1337.bauction.db.kernel.JsonDBCore;
import org.by1337.bauction.db.kernel.JsonDBCoreV2;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;
import org.by1337.bauction.util.TagUtil;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class DataBase {

    private final List<MemorySellItem> sellItems = new ArrayList<>();
    private final List<MemoryUnsoldItem> unsoldItems = new ArrayList<>();
    private final StorageMap<UUID, MemoryUser> users = new StorageMap<>();

    private final StorageMap<NameKey, List<SortingItems>> map = new StorageMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private final Map<NameKey, Category> categoryMap;
    private final Map<NameKey, Sorting> sortingMap;

    private final DBCore core;

    public DataBase(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap) {
        core = new JsonDBCoreV2(this::update);
        this.categoryMap = categoryMap;
        this.sortingMap = sortingMap;
        for (Category category : categoryMap.values()) {
            List<SortingItems> list = new ArrayList<>();
            for (Sorting sorting : sortingMap.values()) {
                list.add(new SortingItems(sorting));
            }
            map.put(category.nameKey(), list);
        }

        writeLock(() -> {
            unsoldItems.addAll(core.getAddUnsoldItems());
            sellItems.addAll(core.getAllSellItems());
            for (MemoryUser user : core.getAllUsers()) {
                users.put(user.getUuid(), user);
            }
            sellItems.forEach(sellItem -> {
                for (Category value : categoryMap.values()) {
                    if (TagUtil.matchesCategory(value, sellItem)) {
                        map.get(value.nameKey()).forEach(list -> list.addItem(sellItem));
                    }
                }
            });
            return null;
        });
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void join(PlayerJoinEvent event) {
                if (readLock(() -> users.containsKey(event.getPlayer().getUniqueId()))) {
                    MemoryUser memoryUser = getMemoryUser(event.getPlayer().getUniqueId());
                    writeLock(() -> {
                        Main.getCfg().getBoostManager().userUpdate(memoryUser);
                        return null;
                    });
                }
            }
        }, Main.getInstance());
    }

    public int getItemsSize() {
        return readLock(sellItems::size);
    }

    public List<MemorySellItem> getAllItems() {
        return readLock(() -> new ArrayList<>(sellItems));
    }

    public void validateAndAddItem(SellItemEvent event) {
        try {
            MemoryUser memoryUser = core.getUser(event.getUser().getUuid());
            Main.getCfg().getBoostManager().userUpdate(memoryUser);

            MemorySellItem sellItem = event.getSellItem();

            if (Main.getCfg().getMaxSlots() <= (memoryUser.getItemForSale().size() - memoryUser.getExternalSlots())) {
                event.setValid(false);
                event.setReason("&cВы достигли лимита по количеству предметов на аукционе!");
                return;
            }
            core.addItem(sellItem, memoryUser.getUuid());
            event.setValid(true);
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
        }
    }

    public void addItem(MemorySellItem sellItem, Player player) {
        writeLock(() -> {
            if (!core.hasUser(player.getUniqueId())) {
                core.createNew(player.getUniqueId(), player.getName());
            }
            core.addItem(sellItem, player.getUniqueId());
            return null;
        });
    }

    public MemoryUser getMemoryUser(UUID uuid) {
        return readLock(() -> Main.getCfg().getBoostManager().userUpdate(users.getOrThrow(uuid, StorageException.ItemNotFoundException::new)));
    }

    public MemorySellItem getMemorySellItem(UUID uuid) {
        return readLock(() -> sellItems.stream().filter(i -> i.getUuid().equals(uuid)).findFirst().orElseThrow(StorageException.ItemNotFoundException::new));
    }

    public boolean hasMemorySellItem(UUID uuid) {
        try {
            return core.hasSellItem(uuid);
        } catch (Exception e) {
            Main.getMessage().error(e);
            return false;
        }
    }

    public void validateAndRemoveItem(TakeItemEvent event) {
        MemoryUser user = event.getUser();
        MemorySellItem sellItem = event.getSellItem();

        if (!user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("&cВы не владелец предмета!");
            return;
        }

        try {
            core.tryRemoveItem(sellItem.getUuid());
        } catch (StorageException e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            return;
        }
        event.setValid(true);
    }

    public void validateAndRemoveItem(TakeUnsoldItemEvent event) {
        try {
            MemoryUser user = core.getUser(event.getUser().getUuid());
            MemoryUnsoldItem unsoldItem = event.getUnsoldItem();

            if (!user.getUuid().equals(unsoldItem.getOwner())) {
                event.setValid(false);
                event.setReason("&cВы не владелец предмета!");
                return;
            } else if (user.getUnsoldItems().stream().noneMatch(i -> i.equals(unsoldItem.getUuid()))) {
                event.setValid(false);
                event.setReason("&cКажется такого предмета нет!");
                return;
            }

            core.tryRemoveUnsoldItem(user.getUuid(), unsoldItem.getUuid());

            event.setValid(true);
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
        }
    }

    public void validateAndRemoveItem(BuyItemEvent event) {
        MemoryUser user = event.getUser();
        MemorySellItem sellItem = event.getSellItem();

        if (user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("&cВы владелец предмета!");
            return;
        }

        try {
            if (!core.hasSellItem(sellItem.getUuid())) {
                event.setValid(false);
                event.setReason("&cПредмет уже продан или снят с продажи!");
                return;
            }

            core.tryRemoveItem(sellItem.getUuid());
        } catch (StorageException e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            return;
        }
        event.setValid(true);
    }

    public List<MemorySellItem> getAllItemByUser(UUID uuid) {
        return readLock(() -> sellItems.stream().filter(i -> i.getSellerUuid().equals(uuid)).toList());
    }

    public List<MemoryUnsoldItem> getAllUnsoldItemsByUser(UUID uuid) {
        return readLock(() -> core);
    }

    public void validateAndRemoveItem(BuyItemCountEvent event) {
        MemoryUser buyer = event.getUser();
        MemorySellItem sellItem = event.getSellItem();

        if (buyer.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("&cВы владелец предмета!");
            return;
        }

        try {
            if (!core.hasSellItem(sellItem.getUuid())) {
                event.setValid(false);
                event.setReason("&cПредмет уже продан или снят с продажи!");
                return;
            }
            MemorySellItem updated = getMemorySellItem(sellItem.getUuid());

            if (updated.getAmount() < event.getCount()) {
                event.setValid(false);
                event.setReason("&cКто-то выкупил часть товара и вы больше не можете купить этот предмет в таком количестве!");
                return;
            }
            core.tryRemoveItem(sellItem.getUuid());

            int newCount = updated.getAmount() - event.getCount();

            if (newCount != 0) {
                MemorySellItem newItem = MemorySellItem.builder()
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
                        .sellFor(updated.getSellFor())
                        .itemStack(updated.getItemStack())
                        .build();

                core.addItem(newItem, buyer.getUuid());
            }

        } catch (StorageException e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            return;
        }
        event.setValid(true);
    }

    public MemoryUser getMemoryUserOrCreate(Player player) {
        return readLock(() -> {
            if (users.containsKey(player.getUniqueId())) {
                return users.get(player.getUniqueId());
            } else if (!core.hasUser(player.getUniqueId())) {
                return core.createNew(player.getUniqueId(), player.getName());
            } else {
                MemoryUser memoryUser = core.getUser(player.getUniqueId());
               // update(new Action<>(ActionType.UPDATE_MEMORY_USER, memoryUser));
                return memoryUser;
            }
        });
    }

    public List<MemorySellItem> getItems(NameKey category, NameKey sorting) {
        return readLock(() -> {
            if (!map.containsKey(category)) {
                throw new IllegalStateException("unknown category: " + category.getName());
            }
            return map.get(category).stream().filter(sortingItems -> sortingItems.getSorting().nameKey().equals(sorting)).findFirst().orElseThrow(() -> new IllegalStateException("unknown sorting: " + sorting.getName())).getItems();

        });
    }

    private void update(Action action) {
        Thread thread = new Thread(() -> {
            if (action.getType() == DBActionType.USER_ADD_SELL_ITEM) {
               MemoryUser user = getMemoryUser(action.getOwner());

            }
        });
        thread.start();
    }

    private void removeIf(Predicate<MemorySellItem> filter) {
        writeLock(() -> {
            sellItems.removeIf(filter);
            Iterator<List<SortingItems>> iterator = map.values().iterator();
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

    public void save() {
        core.save();
    }

    private <T> T writeLock(Task<T> task) {
        lock.writeLock().lock();
        T res = null;
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
        T res = null;
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
