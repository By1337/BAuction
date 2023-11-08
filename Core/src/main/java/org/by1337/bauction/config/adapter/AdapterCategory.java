package org.by1337.bauction.config.adapter;

import org.bukkit.configuration.ConfigurationSection;
import org.by1337.api.configuration.YamlContext;
import org.by1337.api.configuration.adapter.ClassAdapter;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.util.Category;

import java.util.HashSet;

public class AdapterCategory implements ClassAdapter<Category> {
    @Override
    public ConfigurationSection serialize(Category category, YamlContext context) {
        context.set("selected-name", category.selectedName());
        context.set("unselected-name", category.unselectedName());
        context.set("priority", category.priority());
        context.set("tags", category.tags());
        context.set("name", category.nameKey());
        return context.getHandle();
    }

    @Override
    public Category deserialize(YamlContext context) {
        String selectedName = context.getAsString("selected-name");
        String unselectedName = context.getAsString("unselected-name");
        int priority = context.getAsInteger("priority");
        HashSet<String> tags = new HashSet<>(context.getList("tags", String.class));
        NameKey name = context.getAsNameKey("name");
        return new Category(selectedName, unselectedName, priority, tags, name);
    }
}
