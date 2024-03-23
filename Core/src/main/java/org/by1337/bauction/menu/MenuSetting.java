package org.by1337.bauction.menu;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.by1337.bauction.util.OptionParser;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.NameKey;
import org.by1337.bauction.menu.requirement.Requirements;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public class MenuSetting {
    private final List<MenuItemBuilder> items;
    private final String title;
    private final int size;
    private final Requirements viewRequirement;
    private final InventoryType type;
    private final List<String> openCommands;
    private final YamlContext context;
    private final NameKey id;
    private final String provider;

    public MenuSetting(List<MenuItemBuilder> items, String title, int size, Requirements viewRequirement, InventoryType type, List<String> openCommands, YamlContext context, NameKey id) {
        this.items = items;
        this.title = title;
        this.size = size;
        this.viewRequirement = viewRequirement;
        this.type = type;
        this.openCommands = openCommands;
        this.context = context;
        this.id = id;
        provider = Objects.requireNonNull(context.getAsString("provider", null), "provider is null!");
    }

    public YamlContext getContext() {
        return context;
    }

    public List<String> getOpenCommands() {
        return openCommands;
    }


    public List<MenuItemBuilder> getItems() {
        return items;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public Requirements getViewRequirement() {
        return viewRequirement;
    }

    public InventoryType getType() {
        return type;
    }


    public Menu create(Player player, @Nullable Menu backMenu, OptionParser optionParser) {
        var creator = MenuProviderRegistry.getByName(provider);
        if (creator == null) {
            throw new IllegalStateException("unknown provider " + provider);
        }
        return creator.create(this, player, backMenu, optionParser);
    }

    public NameKey getId() {
        return id;
    }
}
