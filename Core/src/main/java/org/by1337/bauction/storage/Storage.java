package org.by1337.bauction.storage;

import com.google.gson.Gson;
import org.bukkit.entity.Player;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.*;
import org.by1337.bauction.storage.event.SellItemEvent;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;
import org.by1337.bauction.util.TagUtil;

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

    }

    public void validateAndAddItem(SellItemEvent event){
        lock.lock();
        try {
            UserImpl user = (UserImpl) event.getUser();
            SellItem sellItem = event.getSellItem();

            if (Main.getCfg().getMaxSlots() <= (user.getItemForSale().size() - user.getExternalSlots())){
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
           if (users.containsKey(owner.getUuid())){
               user = users.get(owner.getUuid());
           }else {
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

    public void editUser(EditSession<EditableUser> session, UUID uuid){
        lock.lock();
        try {
            EditableUserImpl editableUser = new EditableUserImpl((UserImpl) getUser(uuid));
            session.run(editableUser);
            editableUser.setHandle(null);
        } finally {
            lock.unlock();
        }
    }

    public User getUser(UUID uuid){
        lock.lock();
        try {
            if (!users.containsKey(uuid)){
                throw new IllegalStateException("unknown user!");
            }
            return users.get(uuid);
        } finally {
            lock.unlock();
        }
    }
    public User getUserOrCreate(Player player){
        lock.lock();
        try {
            if (!users.containsKey(player.getUniqueId())){
                return createUserAsync(player);
            }
            return users.get(player.getUniqueId());
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

    public User createUserAsync(Player player){
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

    public void save() {
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
