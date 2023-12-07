package org.by1337.bauction.util;

import org.by1337.api.util.NameKey;
import org.by1337.bauction.auc.SellItem;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;

public class Category implements Comparable<Category> {
    private String selectedName;
    private String unselectedName;
    private int priority;
    private HashSet<String> tags;
    private NameKey nameKey;

    public Category(String selectedName, String unselectedName, int priority, HashSet<String> tags, NameKey nameKey) {
        this.selectedName = selectedName;
        this.unselectedName = unselectedName;
        this.priority = priority;
        this.tags = tags;
        this.nameKey = nameKey;
    }

    @Override
    public int compareTo(@NotNull Category o) {
        return Integer.compare(priority, o.priority());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category category)) return false;
        return priority == category.priority && Objects.equals(selectedName, category.selectedName) && Objects.equals(unselectedName, category.unselectedName) && Objects.equals(tags, category.tags) && Objects.equals(nameKey, category.nameKey);
    }

    public String selectedName() {
        return selectedName;
    }

    public String unselectedName() {
        return unselectedName;
    }

    public int priority() {
        return priority;
    }

    public HashSet<String> tags() {
        return tags;
    }

    public NameKey nameKey() {
        return nameKey;
    }

    public void setTags(HashSet<String> tags) {
        this.tags = tags;
    }

    public boolean matches(SellItem sellItem){
        return TagUtil.matchesCategory(this, sellItem);
    }
    @Override
    public int hashCode() {
        return Objects.hash(selectedName, unselectedName, priority, tags, nameKey);
    }

    @Override
    public String toString() {
        return "Category{" +
                "selectedName='" + selectedName + '\'' +
                ", unselectedName='" + unselectedName + '\'' +
                ", priority=" + priority +
                ", tags=" + tags +
                '}';
    }
}
