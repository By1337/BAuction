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

    private YamlContext menu;
    private File fileMenu;

    private YamlContext menuConfirm;
    private File fileMenuConfirm;

    private YamlContext menuBuyCount;
    private File fileMenuBuyCount;

    private YamlContext menuItemsForSale;
    private File fileMenuItemsForSale;

    private YamlContext menuUnsoldItems;
    private File fileMenuUnsoldItems;

    private YamlContext menuPlayerItemsView;
    private File fileMenuPlayerItemsView;

    private Map<NameKey, Sorting> sortingMap;
    private Map<NameKey, Category> categoryMap;

    private int maxSlots;
    private long defaultSellTime;

    private BoostManager boostManager;
    private MenuManger menuManger;
    private boolean allowBuyCount;
    private int itemMaxSize;
    private int compressIfMoreThan;
    private int maximumUncompressedItemSize;

    public Config(Plugin plugin) {
       reload(plugin);
    }

    public void reload(Plugin plugin){
        loadConfigs(plugin);
        sortingMap = new LinkedHashMap<>();
        categoryMap = new LinkedHashMap<>();
        sorting.getMap("sorting", Sorting.class).values().stream().sorted(Sorting::compareTo).forEach(sorting1 -> sortingMap.put(sorting1.nameKey(), sorting1));
        sorting.getMap("categories", Category.class).values().stream().sorted(Category::compareTo).forEach(category -> categoryMap.put(category.nameKey(), category));


        maxSlots = config.getAsInteger("max-slots");
        defaultSellTime = NumberUtil.getTime(config.getAsString("default-offer-time"));

        boostManager = new BoostManager(config);

        menuManger = new MenuManger(this);
        allowBuyCount = config.getAsBoolean("allow-buy-count");
        allowBuyCount = config.getAsBoolean("allow-buy-count");
        itemMaxSize = config.getAsInteger("item-max-size", 70_000);
        compressIfMoreThan = config.getAsInteger("compress-if-more-than", 30_000);
        maximumUncompressedItemSize = config.getAsInteger("maximum-uncompressed-item-size", 350000);
    }

    public void loadConfigs(Plugin plugin){
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

        fileMenu = new File(basedir + "/main.yml");
        if (!fileMenu.exists()) {
            plugin.saveResource("main.yml", true);
        }
        menu = new YamlContext(YamlConfiguration.loadConfiguration(fileMenu));

        fileMenuConfirm = new File(basedir + "/confirm.yml");
        if (!fileMenuConfirm.exists()) {
            plugin.saveResource("confirm.yml", true);
        }
        menuConfirm = new YamlContext(YamlConfiguration.loadConfiguration(fileMenuConfirm));

        fileMenuBuyCount = new File(basedir + "/buyCount.yml");
        if (!fileMenuBuyCount.exists()) {
            plugin.saveResource("buyCount.yml", true);
        }
        menuBuyCount = new YamlContext(YamlConfiguration.loadConfiguration(fileMenuBuyCount));

        fileMenuItemsForSale = new File(basedir + "/itemsForSale.yml");
        if (!fileMenuItemsForSale.exists()) {
            plugin.saveResource("itemsForSale.yml", true);
        }
        menuItemsForSale = new YamlContext(YamlConfiguration.loadConfiguration(fileMenuItemsForSale));

        fileMenuUnsoldItems = new File(basedir + "/unsoldItemList.yml");
        if (!fileMenuUnsoldItems.exists()) {
            plugin.saveResource("unsoldItemList.yml", true);
        }
        menuUnsoldItems = new YamlContext(YamlConfiguration.loadConfiguration(fileMenuUnsoldItems));

        fileMenuPlayerItemsView = new File(basedir + "/playerItemsView.yml");
        if (!fileMenuPlayerItemsView.exists()) {
            plugin.saveResource("playerItemsView.yml", true);
        }
        menuPlayerItemsView = new YamlContext(YamlConfiguration.loadConfiguration(fileMenuPlayerItemsView));

        File readMe = new File(basedir + "/README.yml");
        if (!readMe.exists()) {
            plugin.saveResource("README.yml", true);
        }
    }

    public YamlContext getMenuUnsoldItems() {
        return menuUnsoldItems;
    }

    public MenuManger getMenuManger() {
        return menuManger;
    }

    public YamlContext getMenuItemsForSale() {
        return menuItemsForSale;
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

    public YamlContext getMenu() {
        return menu;
    }

    public YamlContext getMenuConfirm() {
        return menuConfirm;
    }

    public YamlContext getMenuBuyCount() {
        return menuBuyCount;
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

    public YamlContext getMenuPlayerItemsView() {
        return menuPlayerItemsView;
    }

    public File getFileMenuPlayerItemsView() {
        return fileMenuPlayerItemsView;
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
}
