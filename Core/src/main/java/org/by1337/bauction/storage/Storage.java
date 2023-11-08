package org.by1337.bauction.storage;

import org.by1337.api.util.NameKey;
import org.by1337.bauction.Main;
import org.by1337.bauction.SellItem;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;
import org.by1337.bauction.util.TagUtil;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

public class Storage {
    private final List<SellItem> sellItems = new ArrayList<>();

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

    public void addItem(SellItem sellItem) {
        lock.lock();
        try {
            sellItems.add(sellItem);

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

    public List<SellItem> getAllItems() {
        lock.lock();
        try {
            return sellItems;
        } finally {
            lock.unlock();
        }
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
}
