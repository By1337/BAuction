package org.by1337.bauction.db;

import org.by1337.blib.util.NameKey;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.util.auction.Sorting;

import java.util.*;

public class SortingItems {

    private TreeSet<SellItem> items;
    private final NameKey sortingName;
    private final Sorting sorting;
    private final Comparator<SellItem> comparator;

    public SortingItems(Sorting sorting) {
        sortingName = sorting.nameKey();
        this.sorting = sorting;
        comparator = sorting.getComparator();
        items = new TreeSet<>((o1, o2) -> {
            int res = comparator.compare(o1, o2);
            if (res == 0) return o1.getUniqueName().getKey().compareTo(o2.getUniqueName().getKey());
            return res;
        });
    }

    public void addItem(SellItem sellItem) {
        items.add(sellItem);
    }

    public void remove(SellItem sellItem) {
        items.remove(sellItem);
    }

    public void clear() {
        items.clear();
    }

    public Collection<SellItem> getItems() {
        return items;
    }
    public int size(){
        return items.size();
    }

    public NameKey getSortingName() {
        return sortingName;
    }

    public Sorting getSorting() {
        return sorting;
    }
}
