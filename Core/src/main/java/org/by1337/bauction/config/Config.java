package org.by1337.bauction.config;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.api.configuration.YamlContext;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.boost.BoostManager;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.Sorting;

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

    private File itemsDataFolder;

    private Map<NameKey, Sorting> sortingMap;
    private Map<NameKey, Category> categoryMap;

    private int maxSlots;
    private long defaultSellTime;

    private BoostManager boostManager;

    private MenuManger menuManger;

    public Config(Plugin plugin) {
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


        sortingMap = new LinkedHashMap<>();
        categoryMap = new LinkedHashMap<>();
        sorting.getMap("sorting", Sorting.class).values().stream().sorted(Sorting::compareTo).forEach(sorting1 -> sortingMap.put(sorting1.nameKey(), sorting1));
        sorting.getMap("categories", Category.class).values().stream().sorted(Category::compareTo).forEach(category -> categoryMap.put(category.nameKey(), category));


        maxSlots = config.getAsInteger("max-slots");
        defaultSellTime = NumberUtil.getTime(config.getAsString("default-offer-time"));

        boostManager = new BoostManager(config);

        menuManger = new MenuManger(this);
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

    public File getItemsDataFolder() {
        return itemsDataFolder;
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
}
