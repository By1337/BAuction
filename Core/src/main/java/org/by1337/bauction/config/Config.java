package org.by1337.bauction.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.bauction.boost.BoostManager;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.Sorting;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.NameKey;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public class Config {
    private YamlContext message;
    private File messageFile;

    private YamlContext sorting;
    private File sortingFile;

    private YamlContext config;
    private File configFile;
    private Map<NameKey, Sorting> sortingMap;
    private Map<NameKey, Category> categoryMap;
    private int maxSlots;
    private long defaultSellTime;
    private BoostManager boostManager;
    private boolean allowBuyCount;
    private int itemMaxSize;
    private int compressIfMoreThan;
    private int maximumUncompressedItemSize;
    private boolean logging;
    private String homeMenu;
    private String playerItemsViewMenu;
    private String lang;

    public Config(Plugin plugin) {
        reload(plugin);
    }

    public void reload(Plugin plugin) {
        loadConfigs(plugin);
        sortingMap = new LinkedHashMap<>();
        categoryMap = new LinkedHashMap<>();
        sorting.getMap("sorting", Sorting.class).values().stream().sorted(Sorting::compareTo).forEach(sorting1 -> sortingMap.put(sorting1.nameKey(), sorting1));
        sorting.getMap("categories", Category.class).values().stream().sorted(Category::compareTo).forEach(category -> categoryMap.put(category.nameKey(), category));


        maxSlots = config.getAsInteger("default-slots");
        defaultSellTime = NumberUtil.getTime(config.getAsString("default-offer-time"));

        boostManager = new BoostManager(config);

        allowBuyCount = config.getAsBoolean("allow-buy-count");
        allowBuyCount = config.getAsBoolean("allow-buy-count");
        itemMaxSize = config.getAsInteger("item-max-size", 70_000);
        compressIfMoreThan = config.getAsInteger("compress-if-more-than", 30_000);
        maximumUncompressedItemSize = config.getAsInteger("maximum-uncompressed-item-size", 350000);
        logging = config.getAsBoolean("logging", false);
        homeMenu = config.getAsString("home-menu");
        playerItemsViewMenu = config.getAsString("player-items-view-menu");
        lang = config.getAsString("lang", "en_us");

    }

    public void loadConfigs(Plugin plugin) {
        String basedir = plugin.getDataFolder().getPath();

        messageFile = new File(basedir + "/message.yml");
        if (!messageFile.exists()) {
            plugin.saveResource("message.yml", true);
        }
        message = new YamlContext(YamlConfiguration.loadConfiguration(messageFile));

        sortingFile = new File(basedir + "/sorting.yml");
        if (!sortingFile.exists()) {
            plugin.saveResource("sorting.yml", true);
        }
        sorting = new YamlContext(YamlConfiguration.loadConfiguration(sortingFile));

        configFile = new File(basedir + "/config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", true);
        }
        config = new YamlContext(YamlConfiguration.loadConfiguration(configFile));

        File readMe = new File(basedir + "/README.yml");
        if (!readMe.exists()) {
            plugin.saveResource("README.yml", true);
        }
    }


    public YamlContext getMessage() {
        return message;
    }

    public YamlContext getSorting() {
        return sorting;
    }

    public YamlContext getConfig() {
        return config;
    }

    public Map<NameKey, Sorting> getSortingMap() {
        return sortingMap;
    }

    public Map<NameKey, Category> getCategoryMap() {
        return categoryMap;
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
    public boolean isAllowBuyCount() {
        return allowBuyCount;
    }

    public int getItemMaxSize() {
        return itemMaxSize;
    }

    public int getCompressIfMoreThan() {
        return compressIfMoreThan;
    }

    public int getMaximumUncompressedItemSize() {
        return maximumUncompressedItemSize;
    }

    public boolean isLogging() {
        return logging;
    }

    public String getHomeMenu() {
        return homeMenu;
    }

    public String getPlayerItemsViewMenu() {
        return playerItemsViewMenu;
    }

    public String getLang() {
        return lang;
    }
}
