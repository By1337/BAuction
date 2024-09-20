package org.by1337.bauction.util.auction;

import org.by1337.blib.util.NameKey;
import org.by1337.bauction.db.kernel.PluginSellItem;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Set;

public class Category implements Comparable<Category> {
    private String selectedName;
    private String unselectedName;
    private int priority;
    private Set<String> tags;
    private NameKey nameKey;
    private boolean soft = false;

    public Category(String selectedName, String unselectedName, int priority, Set<String> tags, NameKey nameKey) {
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

    public Set<String> tags() {
        return tags;
    }

    public NameKey nameKey() {
        return nameKey;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public boolean matches(PluginSellItem sellItem){
        return TagUtil.matchesCategory(this, sellItem);
    }
    @Override
    public int hashCode() {
        return Objects.hash(selectedName, unselectedName, priority, tags, nameKey);
    }

    public boolean isSoft() {
        return soft;
    }

    public void setSoft(boolean soft) {
        this.soft = soft;
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
