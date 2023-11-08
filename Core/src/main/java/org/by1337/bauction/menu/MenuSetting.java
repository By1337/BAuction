package org.by1337.bauction.menu;

import org.bukkit.configuration.file.FileConfiguration;
import org.by1337.bauction.menu.requirement.Requirements;

import java.util.LinkedList;

public class MenuSetting {
    private LinkedList<CustomItemStack> items;
    private String title;
    private int size;
    private int updateInterval;
    private FileConfiguration menuFile;
    private Requirements viewRequirement;

    public MenuSetting(LinkedList<CustomItemStack> items, String title, int size, int updateInterval, FileConfiguration menuFile, Requirements viewRequirement) {
        this.items = items;
        this.title = title;
        this.size = size;
        this.updateInterval = updateInterval;
        this.menuFile = menuFile;
        this.viewRequirement = viewRequirement;
    }

    public LinkedList<CustomItemStack> getItems() {
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

    public FileConfiguration getMenuFile() {
        return menuFile;
    }

    public Requirements getViewRequirement() {
        return viewRequirement;
    }
}
