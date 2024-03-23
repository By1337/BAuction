package org.by1337.bauction.menu;

import org.bukkit.entity.Player;
import org.by1337.bauction.menu.requirement.Requirement;
import org.by1337.bauction.menu.requirement.RequirementType;
import org.by1337.bauction.util.ImmutableArrayList;
import org.by1337.blib.chat.Placeholderable;

import java.util.Map;

public class ItemSelector {
    private final ImmutableArrayList<SelectorRequirement> requirements;
    private final Map<String, MenuItemBuilder> items;
    private final String defaultItem;

    public ItemSelector(ImmutableArrayList<SelectorRequirement> requirements, Map<String, MenuItemBuilder> items, String defaultItem) {
        this.requirements = requirements;
        this.items = items;
        this.defaultItem = defaultItem;
    }

    public MenuItemBuilder get(Placeholderable placeholderable, Player to) {
        for (SelectorRequirement requirement : requirements) {
            if (requirement.test(placeholderable, to)){
                return items.get(requirement.returnItem);
            }
        }
        return items.get(defaultItem);
    }

    public static class SelectorRequirement implements Requirement {
        final Requirement source;
        final String returnItem;

        public SelectorRequirement(Requirement source, String returnItem) {
            this.source = source;
            this.returnItem = returnItem;
        }

        @Override
        public boolean test(Placeholderable placeholderable, Player clicker) {
            return source.test(placeholderable, clicker);
        }

        @Override
        public RequirementType getType() {
            return source.getType();
        }
    }
}
