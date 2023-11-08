package org.by1337.bauction.util;

import org.by1337.api.util.NameKey;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Objects;

public record Category(String selectedName, String unselectedName, int priority,
                       HashSet<String> tags, NameKey nameKey) implements Comparable<Category> {

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
