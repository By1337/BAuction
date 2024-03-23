package org.by1337.bauction.config.adapter;

import org.bukkit.configuration.ConfigurationSection;
import org.by1337.bauction.menu.ItemSelector;
import org.by1337.bauction.menu.MenuItemBuilder;
import org.by1337.bauction.menu.requirement.Requirement;
import org.by1337.bauction.util.ImmutableArrayList;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.adapter.AdapterRegistry;
import org.by1337.blib.configuration.adapter.ClassAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemSelectorAdapter implements ClassAdapter<ItemSelector> {
    @Override
    public ConfigurationSection serialize(ItemSelector obj, YamlContext context) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ItemSelector deserialize(YamlContext context) {
        Map<String, MenuItemBuilder> items = context.getMap("items", MenuItemBuilder.class);
        Map<String, YamlContext> rawReq = context.getMap("selector", YamlContext.class);
        String defaultItem = null;
        List<ItemSelector.SelectorRequirement> requirements = new ArrayList<>();
        for (Map.Entry<String, YamlContext> entry : rawReq.entrySet()) {
            String k = entry.getKey();
            YamlContext v = entry.getValue();
            if (k.equals("default")) {
                defaultItem = v.getAsString("return-item");
            } else {
                Requirement requirement = AdapterRegistry.getAs(v.getHandle(), Requirement.class);
                String returnItem = v.getAsString("return-item");
                requirements.add(new ItemSelector.SelectorRequirement(requirement, returnItem));
            }
        }
        Objects.requireNonNull(defaultItem, "`default.return-item` is null!");

        return new ItemSelector(new ImmutableArrayList<>(requirements), items, defaultItem);
    }
}
