package org.by1337.bauction.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.api.configuration.YamlContext;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.booost.BoostManager;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.Sorting;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config {
    private FileConfiguration message;
    private File messageFile;

    private FileConfiguration sorting;
    private File sortingFile;

    private FileConfiguration config;
    private File configFile;

    private FileConfiguration menu;
    private File fileMenu;

    private FileConfiguration menuConfirm;
    private File fileMenuConfirm;

    private FileConfiguration menuBuyCount;
    private File fileMenuBuyCount;

    private File itemsDataFolder;

    private Map<NameKey, Sorting> sortingMap;
    private Map<NameKey, Category> categoryMap;

    private int maxSlots;
    private long defaultSellTime;

    private BoostManager boostManager;


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

        fileMenuConfirm = new File(basedir + "/confirm.yml");
        if (!fileMenuConfirm.exists()) {
            plugin.saveResource("confirm.yml", true);
        }
        menuConfirm = YamlConfiguration.loadConfiguration(fileMenuConfirm);

        fileMenuBuyCount = new File(basedir + "/buyCount.yml");
        if (!fileMenuBuyCount.exists()) {
            plugin.saveResource("buyCount.yml", true);
        }
        menuBuyCount = YamlConfiguration.loadConfiguration(fileMenuBuyCount);

        itemsDataFolder = new File(basedir + "/items");
        if (!itemsDataFolder.exists()) {
            itemsDataFolder.mkdir();
        }

        YamlContext context = new YamlContext(sorting);
        sortingMap = new LinkedHashMap<>();
        categoryMap = new LinkedHashMap<>();
        context.getMap("sorting", Sorting.class).values().stream().sorted(Sorting::compareTo).forEach(sorting1 -> sortingMap.put(sorting1.nameKey(), sorting1));
        context.getMap("categories", Category.class).values().stream().sorted(Category::compareTo).forEach(category -> categoryMap.put(category.nameKey(), category));


        YamlContext cfg = new YamlContext(config);
        maxSlots = cfg.getAsInteger("max-slots");
        defaultSellTime = NumberUtil.getTime(cfg.getAsString("default-offer-time"));

        boostManager = new BoostManager(cfg);
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

    public File getItemsDataFolder() {
        return itemsDataFolder;
    }

    public int getMaxSlots() {
        return maxSlots;
    }

    public long getDefaultSellTime() {
        return defaultSellTime;
    }

    public BoostManager getBoostManager() {
        return boostManager;
    }

    public FileConfiguration getMenuConfirm() {
        return menuConfirm;
    }

    public FileConfiguration getMenuBuyCount() {
        return menuBuyCount;
    }
}
