package org.by1337.bauction.db.kernel;

import java.util.*;
import java.util.function.Supplier;

class SellItemList {
    private final Map<UUID, SellItem> map = new HashMap<>();
    private final ArrayList<SellItem> list = new ArrayList<>();
    private final Comparator<SellItem> comparator = Comparator.comparingLong(i -> i.removalDate);


    public void push(List<SellItem> list){
        this.list.addAll(list);
        for (SellItem item : list) {
            map.put(item.uuid, item);
        }
        this.list.sort(comparator);
    }

    public <X extends Throwable> SellItem getOrThrow(UUID key, Supplier<? extends X> def) throws X {
        SellItem value = map.get(key);
        if (value == null) {
            throw def.get();
        }
        return value;
    }

    public SellItem get(UUID key) {
        return map.get(key);
    }

    public boolean containsKey(UUID key) {
        return map.containsKey(key);
    }

    public <T extends SellItem> void put(UUID key, T value) {
        map.put(key, value);

        int insertIndex = Collections.binarySearch(list, value, comparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        list.add(insertIndex, value);
    }

    public void remove(UUID key) {
        SellItem val = map.get(key);
        map.remove(key);
        list.remove(val);
    }

    public Map<UUID, SellItem> getMap() {
        return map;
    }

    public ArrayList<SellItem> getList() {
        return list;
    }

    public Comparator<SellItem> getComparator() {
        return comparator;
    }
}
