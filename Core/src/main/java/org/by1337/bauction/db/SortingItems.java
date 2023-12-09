package org.by1337.bauction.db;

import org.by1337.api.util.NameKey;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.util.Sorting;

import java.util.*;
import java.util.function.Predicate;

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
            if (res == 0) return Arrays.compare(o1.getUniqueName().getKey().toCharArray(), o2.getUniqueName().getKey().toCharArray());
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

    public NameKey getSortingName() {
        return sortingName;
    }

    public Sorting getSorting() {
        return sorting;
    }
}
