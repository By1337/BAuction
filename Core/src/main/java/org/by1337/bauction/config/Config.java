package org.by1337.bauction.config;

import org.by1337.bauction.boost.BoostManager;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.config.ConfigUtil;
import org.by1337.bauction.util.common.NumberUtil;
import org.by1337.bauction.util.auction.Sorting;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.NameKey;

import java.util.LinkedHashMap;
import java.util.Map;

public class Config {
    private YamlContext message;
    private YamlContext sorting;
    private YamlContext config;
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

    public Config( ) {
        reload();
    }

    public void reload() {
        loadConfigs();
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
        lang = config.getAsString("lang", "en");

    }

    public void loadConfigs() {
        message = ConfigUtil.load("message.yml");
        sorting = ConfigUtil.load("sorting.yml");
        config = ConfigUtil.load("config.yml");
        ConfigUtil.trySave("menu/README.yml1");
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
