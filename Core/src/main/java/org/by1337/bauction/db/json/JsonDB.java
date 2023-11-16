package org.by1337.bauction.db.json;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.Main;
import org.by1337.bauction.booost.BoostManager;
import org.by1337.bauction.db.*;
import org.by1337.bauction.db.event.*;
import org.by1337.bauction.db.json.kernel.ActionType;
import org.by1337.bauction.db.json.kernel.DBCore;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;
import org.by1337.bauction.util.TagUtil;

import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class JsonDB extends DBCore {

    private final List<MemorySellItem> sellItems = new ArrayList<>();
    private final StorageMap<UUID, MemoryUser> users = new StorageMap<>();

    private final StorageMap<NameKey, List<SortingItems>> map = new StorageMap<>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();


    private final Map<NameKey, Category> categoryMap;
    private final Map<NameKey, Sorting> sortingMap;

    public JsonDB(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap) {
        super();
        this.categoryMap = categoryMap;
        this.sortingMap = sortingMap;
        for (Category category : categoryMap.values()) {
            List<SortingItems> list = new ArrayList<>();
            for (Sorting sorting : sortingMap.values()) {
                list.add(new SortingItems(sorting));
            }
            map.put(category.nameKey(), list);
        }

        // parse bd
        writeLock(() -> sellItems.addAll(super.getAllSellItems()));
        writeLock(() -> {
            for (MemoryUser user : super.getAllUsers()) {
                users.put(user.getUuid(), user);
            }
            return null;
        });
        // sorting
        writeLock(() -> {
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

    public List<MemorySellItem> getAllItems() {
        return readLock(() -> sellItems);
    }

    public void validateAndAddItem(SellItemEvent event) {
        try {
            MemoryUser memoryUser = super.getUser(event.getUser().getUuid());
            Main.getCfg().getBoostManager().userUpdate(memoryUser);

            MemorySellItem sellItem = event.getSellItem();

            if (Main.getCfg().getMaxSlots() <= (memoryUser.getItemForSale().size() - memoryUser.getExternalSlots())) {
                event.setValid(false);
                event.setReason("&cВы достигли лимита по количеству предметов на аукционе!");
                return;
            }
            super.addItem(sellItem, memoryUser.getUuid());
            event.setValid(true);
        } catch (Exception e) {
            Main.getMessage().error(e);
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
        }
    }

    public void addItem(MemorySellItem sellItem, Player player) {
        writeLock(() -> {
            if (!super.hasUser(player.getUniqueId())) {
                super.createNew(player.getUniqueId(), player.getName());
            }
            super.addItem(sellItem, player.getUniqueId());
            return null;
        });
    }

    public MemoryUser getMemoryUser(UUID uuid) {
        return readLock(() -> Main.getCfg().getBoostManager().userUpdate(users.getOrThrow(uuid, StorageException.NotFoundException::new)));
    }

    public MemorySellItem getMemorySellItem(UUID uuid) {
        return readLock(() -> sellItems.stream().filter(i -> i.getUuid().equals(uuid)).findFirst().orElseThrow(StorageException.NotFoundException::new));
    }

    public boolean hasMemorySellItem(UUID uuid) {
        try {
            return hasSellItem(uuid);
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
            tryRemoveItem(sellItem.getUuid());
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
            MemoryUser user = getUser(event.getUser().getUuid());
            MemoryUnsoldItem unsoldItem = event.getUnsoldItem();

            if (!user.getUuid().equals(unsoldItem.getOwner())) {
                event.setValid(false);
                event.setReason("&cВы не владелец предмета!");
                return;
            } else if (user.getUnsoldItems().stream().noneMatch(i -> i.getUuid().equals(unsoldItem.getUuid()))) {
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
        MemoryUser user = event.getUser();
        MemorySellItem sellItem = event.getSellItem();

        if (user.getUuid().equals(sellItem.getSellerUuid())) {
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

            tryRemoveItem(sellItem.getUuid());
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
        return readLock(() -> users.get(uuid).getUnsoldItems());
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
            if (!hasSellItem(sellItem.getUuid())) {
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
            tryRemoveItem(sellItem.getUuid());

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

                addItem(newItem, buyer.getUuid());
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
            } else if (!super.hasUser(player.getUniqueId())) {
                return super.createNew(player.getUniqueId(), player.getName());
            } else {
                MemoryUser memoryUser = super.getUser(player.getUniqueId());
                update(new Action<>(ActionType.UPDATE_MEMORY_USER, memoryUser));
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

    @Override
    protected void update(Action<?> action) {
        Thread thread = new Thread(() -> {
            if (action.getType() == ActionType.UPDATE_MEMORY_USER) {
                MemoryUser memoryUser = (MemoryUser) action.getBody();
                Main.getCfg().getBoostManager().userUpdate(memoryUser);
                writeLock(() -> {
                    users.put(memoryUser.getUuid(), memoryUser);
                    return null;
                });
            }
            if (action.getType() == ActionType.UPDATE_MEMORY_SELL_ITEM) {
                MemorySellItem sellItem = (MemorySellItem) action.getBody();
                writeLock(() -> {
                    sellItems.add(sellItem);
                    for (Category value : categoryMap.values()) {
                        if (TagUtil.matchesCategory(value, sellItem)) {
                            map.get(value.nameKey()).forEach(list -> list.addItem(sellItem));
                        }
                    }
                    return null;
                });
            }
            if (action.getType() == ActionType.REMOVE_SELL_ITEM) {
                UUID uuid = ((Action<UUID>) action).getBody();
                removeIf(i -> i.getUuid().equals(uuid));
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
