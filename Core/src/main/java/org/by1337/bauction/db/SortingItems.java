package org.by1337.bauction.db;

import org.by1337.api.util.NameKey;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.util.Sorting;

import java.util.*;
import java.util.function.Predicate;

public class SortingItems {

    private List<SellItem> items = new ArrayList<>();
    private final NameKey sortingName;
    private final Sorting sorting;
    private final Comparator<SellItem> comparator;

    public SortingItems(Sorting sorting) {
        sortingName = sorting.nameKey();
        this.sorting = sorting;
        comparator = sorting.getComparator();
    }

    public void addItem(SellItem sellItem) {
        int insertIndex = Collections.binarySearch(items, sellItem, comparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        items.add(insertIndex, sellItem);

    }

    public void sort() {
        items.sort(comparator);
    }

    public void removeIf(Predicate<SellItem> filter) {
        items.removeIf(filter);
    }

    public List<SellItem> getItems() {
        return items;
    }

    public NameKey getSortingName() {
        return sortingName;
    }

    public Sorting getSorting() {
        return sorting;
    }
}
