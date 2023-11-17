package org.by1337.bauction.db.kernel;

import java.util.*;

public class UnsoldItemList {
    private final Map<UUID, UnsoldItem> map1 = new HashMap<>();
    private final Map<UUID, List<UnsoldItem>> map = new HashMap<>();
    private final ArrayList<UnsoldItem> list = new ArrayList<>();
    private final Comparator<UnsoldItem> comparator = Comparator.comparingLong(i -> i.deleteVia);

    public UnsoldItemList() {
    }

    void push(List<UnsoldItem> list) {
        for (UnsoldItem item : list) {
            map1.put(item.uuid, item);

            List<UnsoldItem> list1 = map.get(item.owner);
            if (list1 == null) list1 = new ArrayList<>();
            list1.add(item);
            map.put(item.owner, list1);

            list.add(item);
        }
        list.sort(comparator);
    }

    List<UnsoldItem> getAllByUser(UUID uuid) {
        return map.getOrDefault(uuid, new ArrayList<>());
    }

    void remove(UnsoldItem item) {
        map1.remove(item.uuid);
        List<UnsoldItem> list1 = map.get(item.owner);
        if (list1 != null) {
            list1.remove(item);
            map.put(item.owner, list1);
        }
        list.remove(item);
    }

    void put(UnsoldItem item) {
        map1.put(item.uuid, item);
        List<UnsoldItem> list1 = map.get(item.owner);
        if (list1 == null) list1 = new ArrayList<>();
        list1.add(item);
        map.put(item.owner, list1);

        int insertIndex = Collections.binarySearch(list, item, comparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        list.add(insertIndex, item);
    }

}
