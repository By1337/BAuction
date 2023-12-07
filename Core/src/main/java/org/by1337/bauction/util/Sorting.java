package org.by1337.bauction.util;

import org.by1337.api.util.NameKey;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.db.kernel.CSellItem;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;

public record Sorting(SortingType type, String value, String selectedName,
                      String unselectedName, int priority, NameKey nameKey) implements Comparable<Sorting> {

    @Override
    public int compareTo(@NotNull Sorting o) {
        return Integer.compare(priority, o.priority());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Sorting sorting)) return false;
        return priority == sorting.priority && type == sorting.type && Objects.equals(value, sorting.value) && Objects.equals(selectedName, sorting.selectedName) && Objects.equals(unselectedName, sorting.unselectedName) && Objects.equals(nameKey, sorting.nameKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, value, selectedName, unselectedName, priority, nameKey);
    }

    @Override
    public String toString() {
        return "Sorting{" +
                "type=" + type +
                ", value='" + value + '\'' +
                ", selectedName='" + selectedName + '\'' +
                ", unselectedName='" + unselectedName + '\'' +
                ", priority=" + priority +
                '}';
    }

    public Comparator<SellItem> getComparator(){
        if (type == Sorting.SortingType.COMPARE_MAX) {
            return  switch (value){
                case "{price}" -> (item, item1) -> Double.compare(item1.getPrice(), item.getPrice());
                case "{price_for_one}" -> (item, item1) -> Double.compare(item1.getPriceForOne(), item.getPriceForOne());
                case "{sale_time}" -> (item, item1) -> Double.compare((double) item1.getTimeListedForSale() / 1000, (double) item.getTimeListedForSale() / 1000);
                default -> throw new IllegalArgumentException("unknown sorting type: " + this);
            };
        } else {
            return switch (value){
                case "{price}" -> Comparator.comparingDouble(SellItem::getPrice);
                case "{price_for_one}" -> Comparator.comparingDouble(SellItem::getPriceForOne);
                case "{sale_time}" ->
                        Comparator.comparingDouble((SellItem item) -> (double) item.getTimeListedForSale() / 1000);
                default -> throw new IllegalArgumentException("unknown sorting type: " + this);
            };
        }
    }

    public static enum SortingType {
        COMPARE_MAX,
        COMPARE_MIN;

        public static SortingType getByOrdinal(int x) {
            for (SortingType sortingType : values()) {
                if (sortingType.ordinal() == x) {
                    return sortingType;
                }
            }
            return COMPARE_MAX;
        }
    }
}