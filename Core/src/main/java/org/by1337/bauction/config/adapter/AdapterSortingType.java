package org.by1337.bauction.config.adapter;

import org.bukkit.configuration.ConfigurationSection;
import org.by1337.api.configuration.YamlContext;
import org.by1337.api.configuration.adapter.ClassAdapter;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.util.Sorting;

public class AdapterSortingType implements ClassAdapter<Sorting> {
    @Override
    public ConfigurationSection serialize(Sorting sorting, YamlContext yamlContext) {
        yamlContext.set("type", sorting.type());
        yamlContext.set("value", sorting.value());
        yamlContext.set("selected-name", sorting.selectedName());
        yamlContext.set("unselected-name", sorting.unselectedName());
        yamlContext.set("priority", sorting.priority());
        yamlContext.set("name", sorting.nameKey());
        return yamlContext.getHandle();
    }

    @Override
    public Sorting deserialize(YamlContext context) {
        Sorting.SortingType type = context.getAs("type", Sorting.SortingType.class);
        String value = context.getAsString("value");
        String unselectedName = context.getAsString("unselected-name");
        String selectedName = context.getAsString("selected-name");
        int priority = context.getAsInteger("priority");
        NameKey name = context.getAsNameKey("name");
        return new Sorting(type, value, selectedName, unselectedName, priority, name);
    }
}
