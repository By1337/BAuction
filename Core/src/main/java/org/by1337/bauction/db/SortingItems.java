package org.by1337.bauction.db;

import org.by1337.api.util.NameKey;
import org.by1337.bauction.util.Sorting;

import java.util.*;
import java.util.function.Predicate;

public class SortingItems {

    private List<MemorySellItem> items = new ArrayList<>();
    private final NameKey sortingName;
    private final Sorting sorting;
    private final Comparator<MemorySellItem> comparator;

    public SortingItems(Sorting sorting) {
        sortingName = sorting.nameKey();
        this.sorting = sorting;
        comparator = sorting.getComparator();
    }

    public void addItem(MemorySellItem sellItem) {
        int insertIndex = Collections.binarySearch(items, sellItem, comparator);
        if (insertIndex < 0) {
            insertIndex = -insertIndex - 1;
        }
        items.add(insertIndex, sellItem);

    }

    public void sort() {
        items.sort(comparator);
    }

    public void removeIf(Predicate<MemorySellItem> filter) {
        items.removeIf(filter);
       // items.sort(comparator);
    }

    public List<MemorySellItem> getItems() {
        return items;
    }

    public NameKey getSortingName() {
        return sortingName;
    }

    public Sorting getSorting() {
        return sorting;
    }
}
