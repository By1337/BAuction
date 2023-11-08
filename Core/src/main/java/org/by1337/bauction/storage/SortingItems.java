package org.by1337.bauction.storage;

import org.by1337.api.util.NameKey;
import org.by1337.bauction.SellItem;
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
        if (sorting.type() == Sorting.SortingType.COMPARE_MAX) {
            comparator = switch (sorting.value()){
                case "{price}" -> (item, item1) -> Double.compare(item1.getPrice(), item.getPrice());
                case "{price_for_one}" -> (item, item1) -> Double.compare(item1.getPriceForOne(), item.getPriceForOne());
                case "{sale_time}" -> (item, item1) -> Double.compare((double) item1.getTimeListedForSale() / 1000, (double) item.getTimeListedForSale() / 1000);
                default -> throw new IllegalArgumentException("unknown sorting type: " + sorting.value());
            };
           // comparator = (item, item1) -> Double.compare(Double.parseDouble(item1.replace(sorting.value())), Double.parseDouble(item.replace(sorting.value())));
        } else {
            comparator = switch (sorting.value()){
                case "{price}" -> Comparator.comparingDouble(SellItem::getPrice);
                case "{price_for_one}" -> Comparator.comparingDouble(SellItem::getPriceForOne);
                case "{sale_time}" ->
                        Comparator.comparingDouble((SellItem item) -> (double) item.getTimeListedForSale() / 1000);
                default -> throw new IllegalArgumentException("unknown sorting type: " + sorting.value());
            };
           // comparator = Comparator.comparingDouble(item -> Double.parseDouble(item.replace(sorting.value())));
        }

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
        items.sort(comparator);
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
