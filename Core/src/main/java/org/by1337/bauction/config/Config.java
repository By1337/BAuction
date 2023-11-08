package org.by1337.bauction.config;

import com.google.common.collect.ImmutableSortedMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.api.configuration.YamlContext;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

public class Config {
    private FileConfiguration message;
    private File messageFile;

    private FileConfiguration sorting;
    private File sortingFile;

    private FileConfiguration config;
    private File configFile;

    private FileConfiguration menu;
    private File fileMenu;

    private Map<NameKey, Sorting> sortingMap;
    private Map<NameKey, Category> categoryMap;


    public Config(Plugin plugin) {
        String basedir = plugin.getDataFolder().getPath();

        messageFile = new File(basedir + "/message.yml");
        if (!messageFile.exists()) {
            plugin.saveResource("message.yml", true);
        }
        message = YamlConfiguration.loadConfiguration(messageFile);

        sortingFile = new File(basedir + "/sorting.yml");
        if (!sortingFile.exists()) {
            plugin.saveResource("sorting.yml", true);
        }
        sorting = YamlConfiguration.loadConfiguration(sortingFile);


        configFile = new File(basedir + "/config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", true);
        }
        config = YamlConfiguration.loadConfiguration(configFile);

        fileMenu = new File(basedir + "/main.yml");
        if (!fileMenu.exists()) {
            plugin.saveResource("main.yml", true);
        }
        menu = YamlConfiguration.loadConfiguration(fileMenu);

        YamlContext context = new YamlContext(sorting);
        sortingMap = new LinkedHashMap<>();
        categoryMap = new LinkedHashMap<>();
        context.getMap("sorting", Sorting.class).values().stream().sorted(Sorting::compareTo).forEach(sorting1 -> sortingMap.put(sorting1.nameKey(), sorting1));
        context.getMap("categories", Category.class).values().stream().sorted(Category::compareTo).forEach(category -> categoryMap.put(category.nameKey(), category));

    }

    public FileConfiguration getMessage() {
        return message;
    }

    public File getMessageFile() {
        return messageFile;
    }

    public FileConfiguration getSorting() {
        return sorting;
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public Map<NameKey, Sorting> getSortingMap() {
        return sortingMap;
    }

    public Map<NameKey, Category> getCategoryMap() {
        return categoryMap;
    }

    public FileConfiguration getMenu() {
        return menu;
    }
}
