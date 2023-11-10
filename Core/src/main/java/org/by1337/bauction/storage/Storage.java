package org.by1337.bauction.storage;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitTask;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.*;
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
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class Storage {

    private final List<SellItem> sellItems = new ArrayList<>();
    private final Map<UUID, UserImpl> users = new HashMap<>();
    //  private final Map<UUID, MemoryUser> memoryUserMap = new HashMap<>();

    private final Map<NameKey, List<SortingItems>> map = new HashMap<>();

    private final Lock lock = new ReentrantLock();

    private final Map<NameKey, Category> categoryMap;
    private final Map<NameKey, Sorting> sortingMap;

    private BukkitTask updateTask;

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

    @Nullable
    public User update(User user) {
        lock.lock();
        try {
            return users.values().stream().filter(sellItem1 -> user.getUuid().equals(sellItem1.getUuid())).findFirst().orElse(null);
        } catch (Exception e) {
            Main.getMessage().error(e);
            return null;
        } finally {
            lock.unlock();
        }
    }

    @Nullable
    public SellItem update(SellItem item) {
        lock.lock();
        try {
            return sellItems.stream().filter(sellItem1 -> item.getUuid().equals(sellItem1.getUuid())).findFirst().orElse(null);
        } catch (Exception e) {
            Main.getMessage().error(e);
            return null;
        } finally {
            lock.unlock();
        }
    }

    public void validateAndRemoveItem(BuyItemEvent event) {
        lock.lock();
        try {
            UserImpl seller = (UserImpl) getUser(event.getSellItem().getSellerUuid());
            UserImpl user = (UserImpl) getUser(event.getUser().getUuid());
            SellItem item = event.getSellItem();

            SellItem sellItem = sellItems.stream().filter(sellItem1 -> item.getUuid().equals(sellItem1.getUuid())).findFirst().orElse(null);

            if (sellItem == null) {
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
            sellItems.removeIf(i -> i.getUuid().equals(sellItem.getUuid()));
            removeIf(i -> i.getUuid().equals(sellItem.getUuid()));
            seller.addDealCount(1);
            seller.addDealSum((int) item.getPrice());
        } catch (Exception e) {
            Main.getMessage().error(e);
        } finally {
            lock.unlock();
        }
    }

    public void validateAndRemoveItem(TakeItemEvent event) {
        lock.lock();
        try {
            UserImpl user = (UserImpl) getUser(event.getUser().getUuid());
            SellItem item = event.getSellItem();

            SellItem sellItem = sellItems.stream().filter(sellItem1 -> item.getUuid().equals(sellItem1.getUuid())).findFirst().orElse(null);

            if (sellItem == null) {
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
            sellItems.removeIf(i -> i.getUuid().equals(sellItem.getUuid()));
            removeIf(i -> i.getUuid().equals(sellItem.getUuid()));
        } catch (Exception e) {
            Main.getMessage().error(e);
        } finally {
            lock.unlock();
        }
    }

    public void validateAndAddItem(SellItemEvent event) {
        lock.lock();
        try {
            UserImpl user = (UserImpl) getUser(event.getUser().getUuid());
            SellItem sellItem = event.getSellItem();

            if (Main.getCfg().getMaxSlots() <= (user.getItemForSale().size() - user.getExternalSlots())) {
                event.setValid(false);
                event.setReason("&cВы достигли лимита по количеству предметов на аукционе!");
                return;
            }
            event.setValid(true);
            addItem(sellItem, user);

        } catch (Exception e) {
            Main.getMessage().error(e);
        } finally {
            lock.unlock();
        }
    }

    public void addItem(SellItem sellItem, User owner) {
        lock.lock();
        try {
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

        } catch (Exception e) {
            Main.getMessage().error(e);
        } finally {
            lock.unlock();
        }
    }

    public void sort() {
        lock.lock();
        try {
            for (Category value : categoryMap.values()) {
                map.get(value.nameKey()).forEach(SortingItems::sort);
            }
        } catch (Exception e) {
            Main.getMessage().error(e);
        } finally {
            lock.unlock();
        }
    }

    public void removeIf(Predicate<SellItem> filter) {
        lock.lock();
        try {
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
        } finally {
            lock.unlock();
        }
    }

    public void editUser(EditSession<EditableUser> session, UUID uuid) {
        lock.lock();
        try {
            EditableUserImpl editableUser = new EditableUserImpl((UserImpl) getUser(uuid));
            session.run(editableUser);
            editableUser.setHandle(null);
        } finally {
            lock.unlock();
        }
    }

    public User getUser(UUID uuid) {
        lock.lock();
        try {
            if (!users.containsKey(uuid)) {
                throw new IllegalStateException("unknown user!");
            }
            return users.get(uuid);
        } finally {
            lock.unlock();
        }
    }

    public boolean hasUser(UUID uuid) {
        lock.lock();
        try {
            return users.containsKey(uuid);
        } finally {
            lock.unlock();
        }
    }

    public User getUserOrCreate(Player player) {
        lock.lock();
        try {
            if (!users.containsKey(player.getUniqueId())) {
                User user = createUserAsync(player);
                users.put(user.getUuid(), (UserImpl) user);
                return user;
            }
            return users.get(player.getUniqueId());
        } finally {
            lock.unlock();
        }
    }

    public void updateUser(UUID uuid) {
        lock.lock();
        try {
            User user = getUser(uuid);
            Main.getCfg().getBoostManager().userUpdate(user);
            users.put(user.getUuid(), (UserImpl) user);
        } finally {
            lock.unlock();
        }
    }

    public List<SellItem> getAllItems() {
        lock.lock();
        try {
            return sellItems;
        } finally {
            lock.unlock();
        }
    }

    public User createUserAsync(Player player) {
        return new UserImpl(player.getName(), player.getUniqueId());
    }

    public List<SellItem> getItems(NameKey category, NameKey sorting) {
        lock.lock();
        try {
            if (!map.containsKey(category)) {
                throw new IllegalStateException("unknown category: " + category.getName());
            }
            return map.get(category).stream().filter(sortingItems -> sortingItems.getSorting().nameKey().equals(sorting)).findFirst().orElseThrow(() -> new IllegalStateException("unknown sorting: " + sorting.getName())).getItems();

        } finally {
            lock.unlock();
        }
    }

    public void end() {
        save();
        updateTask.cancel();
    }


    private void save() {
        lock.lock();
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
            lock.unlock();
        }

    }

}
