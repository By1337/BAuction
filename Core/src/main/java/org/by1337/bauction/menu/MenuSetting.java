package org.by1337.bauction.menu;

import org.bukkit.configuration.file.FileConfiguration;
import org.by1337.bauction.menu.requirement.Requirements;

import java.util.LinkedList;
import java.util.List;

public class MenuSetting {
    private final List<CustomItemStack> items;
    private final String title;
    private final int size;
    private final int updateInterval;
    private final Requirements viewRequirement;

    public MenuSetting(List<CustomItemStack> items, String title, int size, int updateInterval , Requirements viewRequirement) {
        this.items = items;
        this.title = title;
        this.size = size;
        this.updateInterval = updateInterval;
        this.viewRequirement = viewRequirement;
    }

    public List<CustomItemStack> getItems() {
        return items;
    }

    public String getTitle() {
        return title;
    }

    public int getSize() {
        return size;
    }

    public int getUpdateInterval() {
        return updateInterval;
    }


    public Requirements getViewRequirement() {
        return viewRequirement;
    }
}
