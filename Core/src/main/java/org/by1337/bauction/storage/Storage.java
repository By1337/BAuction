package org.by1337.bauction.storage;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.api.BLib;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.*;
import org.by1337.bauction.storage.event.BuyItemCountEvent;
import org.by1337.bauction.storage.event.BuyItemEvent;
import org.by1337.bauction.storage.event.SellItemEvent;
import org.by1337.bauction.storage.event.TakeItemEvent;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;
import org.by1337.bauction.util.TagUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

public class Storage {

    private final List<SellItem> sellItems = new ArrayList<>();
    private final Map<UUID, UserImpl> users = new HashMap<>();
    private final Map<NameKey, List<SortingItems>> map = new HashMap<>();

    //private final Lock lock = new ReentrantLock();

    private final Map<NameKey, Category> categoryMap;
    private final Map<NameKey, Sorting> sortingMap;

    private BukkitTask updateTask;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Storage(Map<NameKey, Category> categoryMap, Map<NameKey, Sorting> sortingMap) {
        this.categoryMap = categoryMap;
        this.sortingMap = sortingMap;

        for (Category category : categoryMap.values()) {
            List<SortingItems> list = new ArrayList<>();
            for (Sorting sorting : sortingMap.values()) {
                list.add(new SortingItems(sorting));
            }
            map.put(category.nameKey(), list);
        }
        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(Main.getInstance(), this::updateTask, 0, 60 * 20);

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
            public void listen(PlayerJoinEvent event) {
                if (hasUser(event.getPlayer().getUniqueId())) {
                    Main.getCfg().getBoostManager().userUpdate(getUser(event.getPlayer().getUniqueId()));
                }
            }
        }, Main.getInstance());
    }

    private void updateTask() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (hasUser(player.getUniqueId()))
                Main.getCfg().getBoostManager().userUpdate(getUser(player.getUniqueId()));
        }
    }

    private <T> T writeLock(Task<T> task) {
        lock.writeLock().lock();
        try {
            return task.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.writeLock().unlock();
        }
    }

    private <T> T readLock(Task<T> task) {
        lock.readLock().lock();
        try {
            return task.run();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Nullable
    public User update(User user) {
        return readLock(() -> users.values().stream().filter(user1 -> user.getUuid().equals(user1.getUuid())).findFirst().orElse(null));
    }

    @Nullable
    public SellItem update(SellItem item) {
        return readLock(() -> sellItems.stream().filter(sellItem1 -> item.getUuid().equals(sellItem1.getUuid())).findFirst().orElse(null));
    }

    public User getUser(UUID uuid) {
        return readLock(() -> users.get(uuid));
    }

    public void removeItem(UUID uuid) {
        writeLock(() -> {
            sellItems.removeIf(i -> i.getUuid().equals(uuid));
            removeIf(i -> i.getUuid().equals(uuid));
            return null;
        });

    }

    public void validateAndRemoveItem(BuyItemCountEvent event) {
        UserImpl seller = (UserImpl) getUser(event.getSellItem().getSellerUuid());
        UserImpl user = (UserImpl) update(event.getUser());
        SellItem sellItem = update(event.getSellItem());

        if (seller == null) {
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            Main.getMessage().error(new Throwable("lost seller"));
            return;
        } else if (user == null) {
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            Main.getMessage().error(new Throwable("lost user"));
            return;
        } else if (sellItem == null) {
            event.setValid(false);
            event.setReason("&cПредмет уже продан или снят с продажи!");
            return;
        } else if (user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("&cВы владелец предмета!");
            return;
        } else if (!seller.getItemForSale().contains(sellItem.getUuid())) {
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            Main.getMessage().error(new Throwable("lost item"));
            return;
        } else if (sellItem.getAmount() < event.getCount()) {
            event.setValid(false);
            event.setReason("&cВы уже не можете купить это количество предмета");
            return;
        }else if (!sellItem.isSaleByThePiece()){
            event.setValid(false);
            event.setReason("&cПредмет не продаётся по штучно!");
            return;
        }
        event.setValid(true);
        removeItem(sellItem.getUuid());
        ItemStack itemStack = sellItem.getItemStack();
        itemStack.setAmount(itemStack.getAmount() - event.getCount());

        SellItem newSellItem = new SellItem(
                BLib.getApi().getItemStackSerialize().serialize(itemStack),
                sellItem.getSellerName(),
                sellItem.getSellerUuid(),
                sellItem.getPriceForOne() * itemStack.getAmount(),
                true,
                sellItem.getTags(),
                sellItem.getTimeListedForSale(),
                sellItem.getRemovalDate(),
                UUID.randomUUID(),
                sellItem.getMaterial(),
                itemStack.getAmount(),
                sellItem.getPriceForOne(),
                itemStack
        );

        addItem(newSellItem, user);
        editUser((edit) -> {
            edit.addDealCount(1);
            edit.removeSellItem(sellItem.getUuid());
        }, seller.getUuid());

    }

    public void validateAndRemoveItem(BuyItemEvent event) {
        //   readLock(() -> {
        UserImpl seller = (UserImpl) getUser(event.getSellItem().getSellerUuid());
        UserImpl user = (UserImpl) update(event.getUser());
        SellItem sellItem = update(event.getSellItem());

        if (seller == null) {
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            Main.getMessage().error(new Throwable("lost seller"));
            return;
        } else if (user == null) {
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            Main.getMessage().error(new Throwable("lost user"));
            return;
        } else if (sellItem == null) {
            event.setValid(false);
            event.setReason("&cПредмет уже продан или снят с продажи!");
            return;
        } else if (user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("&cВы владелец предмета!");
            return;
        } else if (!seller.getItemForSale().contains(sellItem.getUuid())) {
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            Main.getMessage().error(new Throwable("lost item"));
            return;
        }
        event.setValid(true);
        removeItem(sellItem.getUuid());

        editUser((edit) -> {
            edit.addDealCount(1);
            edit.removeSellItem(sellItem.getUuid());
        }, seller.getUuid());
        //  });
    }

    public void validateAndRemoveItem(TakeItemEvent event) {
        //    readLock(() -> {
        UserImpl user = (UserImpl) getUser(event.getUser().getUuid());
        SellItem sellItem = update(event.getSellItem());

        if (user == null) {
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            Main.getMessage().error(new Throwable("lost user"));
            return;
        } else if (sellItem == null) {
            event.setValid(false);
            event.setReason("&cПредмет уже продан или снят с продажи!");
            return;
        } else if (!user.getUuid().equals(sellItem.getSellerUuid())) {
            event.setValid(false);
            event.setReason("&cВы не владелец предмета!");
            return;
        } else if (!user.getItemForSale().contains(sellItem.getUuid())) {
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            Main.getMessage().error(new Throwable("lost item"));
            return;
        }
        event.setValid(true);
        editUser((edit) -> user.removeSellItem(sellItem.getUuid()), user.getUuid());
        removeItem(sellItem.getUuid());
        return;
        //  });
    }

    public void validateAndAddItem(SellItemEvent event) {
        //     readLock(() -> {
        UserImpl user = (UserImpl) update(event.getUser());
        SellItem sellItem = event.getSellItem();

        if (user == null) {
            event.setValid(false);
            event.setReason("&cПроизошла ошибка!");
            Main.getMessage().error(new Throwable("lost user"));
            return;
        } else if (Main.getCfg().getMaxSlots() <= (user.getItemForSale().size() - user.getExternalSlots())) {
            event.setValid(false);
            event.setReason("&cВы достигли лимита по количеству предметов на аукционе!");
            return;
        }

        event.setValid(true);
        addItem(sellItem, user);
        return;
        //   });
    }

    public void addItem(SellItem sellItem, User owner) {
        writeLock(() -> {
            sellItems.add(sellItem);
            UserImpl user;
            if (users.containsKey(owner.getUuid())) {
                user = users.get(owner.getUuid());
            } else {
                user = (UserImpl) owner;
                users.put(user.getUuid(), user);
            }
            user.addSellItem(sellItem.getUuid());

            for (Category value : categoryMap.values()) {
                if (TagUtil.matchesCategory(value, sellItem)) {
                    map.get(value.nameKey()).forEach(list -> list.addItem(sellItem));
                }
            }
            return null;
        });
    }


    public void removeIf(Predicate<SellItem> filter) {
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

    public void editUser(EditSession<EditableUser> session, UUID uuid) {
        writeLock(() -> {
            EditableUserImpl editableUser = new EditableUserImpl((UserImpl) getUser(uuid));
            session.run(editableUser);
            editableUser.setHandle(null);
            return null;
        });
    }

    public boolean hasUser(UUID uuid) {
        return readLock(() -> users.containsKey(uuid));
    }

    public User getUserOrCreate(Player player) {
        //   return readLock(() -> {
        if (readLock(() -> !users.containsKey(player.getUniqueId()))) {
            writeLock(() -> {
                User user = createUserAsync(player);
                users.put(user.getUuid(), (UserImpl) user);
                return user;
            });
        }
        return readLock(() -> users.get(player.getUniqueId()));
        //  });
    }

    public void updateUser(UUID uuid) {
        writeLock(() -> {
            User user = getUser(uuid);
            Main.getCfg().getBoostManager().userUpdate(user);
            users.put(user.getUuid(), (UserImpl) user);
            return null;
        });
    }

    public List<SellItem> getAllItems() {
        return readLock(() -> sellItems);
    }

    public User createUserAsync(Player player) {
        return new UserImpl(player.getName(), player.getUniqueId());
    }

    public List<SellItem> getItems(NameKey category, NameKey sorting) {
        return readLock(() -> {
            if (!map.containsKey(category)) {
                throw new IllegalStateException("unknown category: " + category.getName());
            }
            return map.get(category).stream().filter(sortingItems -> sortingItems.getSorting().nameKey().equals(sorting)).findFirst().orElseThrow(() -> new IllegalStateException("unknown sorting: " + sorting.getName())).getItems();

        });
    }

    public void end() {
        save();
        updateTask.cancel();
    }


    private void save() {
        lock.readLock().lock();
        try {
            for (File file : Main.getCfg().getItemsDataFolder().listFiles()) {
                file.delete();
            }
            int max = 10000;
            int last = 0;
            int total = sellItems.size();
            List<SellItem> buffer = new ArrayList<>();
            for (int i = 0; i < total; i++) {
                buffer.add(sellItems.get(i));
                if (i - last >= max || i == total - 1) {
                    File file = new File(Main.getCfg().getItemsDataFolder() + "/items-" + (last + 1) + "-" + (i) + ".json");
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
            Main.getMessage().error("failed to save items!", e);
        } finally {
            lock.readLock().unlock();
        }

    }

    @FunctionalInterface
    private interface Task<T> {
        T run();
    }

}
