package org.by1337.bauction;

import com.google.common.base.Joiner;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.boost.Boost;
import org.by1337.bauction.command.*;
import org.by1337.bauction.config.Config;
import org.by1337.bauction.config.adapter.*;
import org.by1337.bauction.datafix.UpdateManager;
import org.by1337.bauction.db.kernel.FileDataBase;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.event.EventManager;
import org.by1337.bauction.hook.EconomyHook;
import org.by1337.bauction.hook.impl.PlayerPointsHook;
import org.by1337.bauction.hook.impl.VaultHook;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.log.FileLogger;
import org.by1337.bauction.log.PluginLogger;
import org.by1337.bauction.menu2.*;
import org.by1337.bauction.placeholder.PlaceholderHook;
import org.by1337.bauction.search.TrieManager;
import org.by1337.bauction.util.*;
import org.by1337.bauction.util.plugin.PluginEnablePipeline;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.adapter.AdapterRegistry;
import org.by1337.blib.configuration.adapter.impl.primitive.AdapterEnum;
import org.by1337.bmenu.BMenuApi;
import org.by1337.bmenu.menu.MenuLoader;
import org.by1337.bmenu.menu.MenuProviderRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public final class Main extends JavaPlugin {
    public static final boolean IS_RELEASE = false;
    private static Message message;
    private static Main instance;
    private static Config cfg;
    private static FileDataBase storage;
    private Command<CommandSender> command;
    private static EconomyHook econ;
    private static TrieManager trieManager;
    private static TimeUtil timeUtil;
    private PlaceholderHook placeholderHook;
    private static UniqueNameGenerator uniqueNameGenerator;
    private static DbCfg dbCfg;
    private boolean loaded;
    private static Set<String> blackList = new HashSet<>();
    private static EventManager eventManager;
    private FileLogger fileLogger;
    private MenuLoader menuLoader;
    private PluginEnablePipeline enablePipeline;
    private Metrics metrics;
    private PluginLogger pluginLogger;
    private static boolean DEBUG_MODE = true;

    @Override
    public void onLoad() {
        instance = this;
        File menus = new File(getDataFolder(), "menu");
        if (!menus.exists()) {
            menus.mkdirs();
            saveResource("menu/home.yml", true);
            saveResource("menu/confirmBuyItem.yml", true);
            saveResource("menu/confirmBuyCountItem.yml", true);
            saveResource("menu/selectCount.yml", true);
            saveResource("menu/itemForSale.yml", true);
            saveResource("menu/unsoldItems.yml", true);
            saveResource("menu/playerItemsView.yml", true);
            saveResource("menu/viewShulkerMenu.yml", true);
        }
        message = new Message(getLogger());
        pluginLogger = new PluginLogger(new File(getDataFolder(), "pluginLogs"), getLogger());
        metrics = new Metrics(this, 20300);
        BMenuApi.setup(message, this);
        MenuProviderRegistry.register("home", HomeMenu::new);
        MenuProviderRegistry.register("itemViewer", ItemViewerMenu::new);
        MenuProviderRegistry.register("selectCount", SelectCountMenu::new);
        MenuProviderRegistry.register("itemsForSale", ItemsForSaleMenu::new);
        MenuProviderRegistry.register("unsoldItems", UnsoldItemsMenu::new);
        MenuProviderRegistry.register("playerItemsView", PlayerItemsView::new);
        MenuProviderRegistry.register("viewShulker", ViewShulkerMenu::new);
        initEnablePipeline();
    }

    private void initEnablePipeline() {
        enablePipeline = new PluginEnablePipeline(this);
        enablePipeline.enable("checkUpdate", UpdateManager::checkUpdate);

        enablePipeline.enable("enable BMenuApi", BMenuApi::enable);
        enablePipeline.enable("load MenuLoader", () -> {
            menuLoader = new MenuLoader(this, new File(getDataFolder(), "menu"), MenuLoader.ResourceLeakDetectorMode.PANIC);
            menuLoader.load();
        });
        enablePipeline.enable("load db cfg", () -> {
            dbCfg = new DbCfg(ConfigUtil.load("dbCfg.yml"));
            dbCfg.validate();
        });
        enablePipeline.enable("registerAdapters", this::registerAdapters);

        enablePipeline.enable("load UniqueNameGenerator", () -> {
            var cfg = ConfigUtil.load("dbCfg.yml");
            int seed = cfg.getAsInteger("name-generator.last-seed");
            uniqueNameGenerator = new UniqueNameGenerator(++seed);
            cfg.set("name-generator.last-seed", seed);
            cfg.save();
        });

        enablePipeline.enable("load lang", () -> {
            Lang.load(this);
        });
        enablePipeline.enable("load cfg", () -> {
            cfg = new Config(this);
        });
        enablePipeline.enable("load logger", () -> {
            if (cfg.isLogging()) {
                fileLogger = new FileLogger(new File(getDataFolder(), "logs"), this);
            }
        });
        enablePipeline.enable("load event manager", () -> {
            eventManager = new EventManager(new YamlContext(YamlConfiguration.loadConfiguration(saveIfNotExist("listener.yml"))));
        });
        enablePipeline.enable("load time util", () -> {
            timeUtil = new TimeUtil();
        });
        enablePipeline.enable("load TrieManager", () -> {
            trieManager = new TrieManager(this);
            TagUtil.loadAliases(this);
        });
        enablePipeline.enable("load econ", () -> {
            String econType = Objects.requireNonNull(cfg.getConfig().getAsString("economy"), "тип экономики не указан!");
            if (econType.equalsIgnoreCase("vault")) {
                econ = new VaultHook();
            } else if (econType.equalsIgnoreCase("playerpoints")) {
                econ = new PlayerPointsHook();
            } else {
                throw new IllegalStateException("Параметр economy имеет не правильное значение! '" + econType + "'. Ожидалось 'Vault' | 'PlayerPoints'");
            }
        });
//        enablePipeline.enable("load metrics", () -> {
//            metrics = new Metrics(this, 20300);
//        });
        enablePipeline.enable("load PAPI hook", () -> {
            placeholderHook = new PlaceholderHook();
            placeholderHook.register();
        });
        enablePipeline.enable("load black list", () -> {
            blackList = new HashSet<>(cfg.getConfig().getList("black-list", String.class, Collections.emptyList()));
        });
        enablePipeline.enable("load db", this::loadDb);
        enablePipeline.enable("check version", VersionChecker::new);

        enablePipeline.enable("init commands", this::initCommand);

        enablePipeline.disable("disable PAPI hook", p -> p.isEnabled("load PAPI hook"), () -> {
            placeholderHook.unregister();
        });
//        enablePipeline.disable("disable metrics", p -> p.isEnabled("load metrics"), () -> {
//            metrics.shutdown();
//        });
        enablePipeline.disable("disable logger", p -> p.isEnabled("load logger"), () -> {
            if (fileLogger != null)
                fileLogger.close();
        });
        enablePipeline.disable("disable BMenuApi", p -> p.isEnabled("enable BMenuApi"), BMenuApi::disable);
        enablePipeline.disable("disable db", p -> p.isEnabled("load db"), () -> {
            try {
                storage.save();
                storage.close();
            } catch (IOException e) {
                message.error("failed to save db", e);
            } finally {
                storage = null;
            }
        });
        enablePipeline.disable("unregisterAdapters", p -> p.isEnabled("registerAdapters"), () -> {
            AdapterRegistry.unregisterPrimitiveAdapter(Sorting.SortingType.class);
            AdapterRegistry.unregisterPrimitiveAdapter(InventoryType.class);
            AdapterRegistry.unregisterAdapter(Sorting.class);
            AdapterRegistry.unregisterAdapter(Category.class);
            AdapterRegistry.unregisterAdapter(Boost.class);
        });
    }

    @Override
    public void onEnable() {
        enablePipeline.onEnable();
    }

    @Override
    public void onDisable() {
        enablePipeline.onDisable();
        if (pluginLogger != null) {
            pluginLogger.close();
        }
    }

    public static void debug(String s, Object... objects) {
        if (DEBUG_MODE) {
            message.log("[DEBUG] " + s, objects);
        } else {
            instance.pluginLogger.log(String.format("[DEBUG] " + s, objects));
        }
    }

    public void loadDb() {
        if (storage != null) {
            throw new IllegalStateException("data base already loaded!");
        }

        if (dbCfg.getDbType() == DbCfg.DbType.MYSQL) {
            ThreadCreator.createThreadWithName("bauc Mysql Db loader", () -> {
                TimeCounter timeCounter = new TimeCounter();

                try {
                    storage = new MysqlDb(cfg.getCategoryMap(), cfg.getSortingMap(), dbCfg);
                    storage.load();
                    loaded = true;
                } catch (IOException | SQLException e) {
                    message.error("failed to load db!", e);
                    instance.getServer().getPluginManager().disablePlugin(instance);
                }
                message.log(Lang.getMessage("successful_loading"), storage.getSellItemsSize(), timeCounter.getTime());

                getCommand("bauc").setTabCompleter(this::onTabComplete0);
                getCommand("bauc").setExecutor(this::onCommand0);
            }).start();
        } else {
            ThreadCreator.createThreadWithName("bauc File Db loader", () -> {
                TimeCounter timeCounter = new TimeCounter();
                storage = new FileDataBase(cfg.getCategoryMap(), cfg.getSortingMap());
                try {
                    storage.load();
                    loaded = true;
                } catch (IOException e) {
                    message.error("failed to load db!", e);
                    instance.getServer().getPluginManager().disablePlugin(instance);
                }
                message.log(Lang.getMessage("successful_loading"), storage.getSellItemsSize(), timeCounter.getTime());
                getCommand("bauc").setTabCompleter(this::onTabComplete0);
                getCommand("bauc").setExecutor(this::onCommand0);
            }).start();
        }
    }


    private void registerAdapters() {
        AdapterRegistry.registerPrimitiveAdapter(Sorting.SortingType.class, new AdapterEnum<>(Sorting.SortingType.class));
        AdapterRegistry.registerPrimitiveAdapter(InventoryType.class, new AdapterEnum<>(InventoryType.class));
        AdapterRegistry.registerAdapter(Sorting.class, new AdapterSortingType());
        AdapterRegistry.registerAdapter(Category.class, new AdapterCategory());
        AdapterRegistry.registerAdapter(Boost.class, new AdapterBoost());
    }

    public void fullReload() {
        enablePipeline.reload();
    }

    public DbCfg getDbCfg() {
        return dbCfg;
    }

    public static UniqueNameGenerator getUniqueNameGenerator() {
        return uniqueNameGenerator;
    }

    public static Message getMessage() {
        return message;
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static Config getCfg() {
        return cfg;
    }

    public static FileDataBase getStorage() {
        return storage;
    }

    public static EconomyHook getEcon() {
        return econ;
    }

    public static TimeUtil getTimeUtil() {
        return timeUtil;
    }

    private void initCommand() {
        command = new Command<CommandSender>("bauc")
                .requires(new RequiresPermission<>("bauc.use"))
                .addSubCommand(new ReloadCmd("reload"))
                .addSubCommand(new Command<CommandSender>("admin")
                        .requires(new RequiresPermission<>("bauc.admin"))
                        .addSubCommand(new Command<CommandSender>("debug")
                                .requires(new RequiresPermission<>("bauc.admin.debug"))
                                .addSubCommand(new PushCmd("push"))
                                .addSubCommand(new RunCmd("run"))
                                .addSubCommand(new PingCmd("ping"))
                                .addSubCommand(new ClearCmd("clear"))
                                .addSubCommand(new StressCmd("stress"))
                        )
                        .addSubCommand(new AddTagCmd("addTag"))
                        .addSubCommand(new OpenCmd("open", menuLoader, cfg.getHomeMenu()))
                        .addSubCommand(new Command<CommandSender>("parse")
                                .requires(new RequiresPermission<>("bauc.parse"))
                                .addSubCommand(new ParseTagsCmd("tags"))
                                .addSubCommand(new ParseNbtCmd("nbt"))
                        )
                )
                .addSubCommand(new SellCmd("sell"))
                .addSubCommand(new SearchCmd("search", menuLoader, cfg.getHomeMenu()))
                .addSubCommand(new ViewCommand("view", menuLoader, cfg.getPlayerItemsViewMenu()))
                .addSubCommand(
                        new Command<CommandSender>("test")
                                .executor(((sender, args) -> {
                                    Player player = (Player) sender;
                                    var menu = menuLoader.getMenu("home");
                                    menu.create(player, null).open();
                                }))

                )

                .executor(((sender, args) -> {
                    if (!(sender instanceof Player player))
                        throw new CommandException(Lang.getMessage("must_be_player"));
                    var menu = menuLoader.getMenu(cfg.getHomeMenu());
                    Objects.requireNonNull(menu, "Menu " + cfg.getHomeMenu() + " not found!");
                    menu.create(player, null).open();
                }))
        ;
    }


    private boolean onCommand0(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
        debug("%s use command %s %s", sender, label, Joiner.on(" ").join(args));
        try {
            command.process(sender, args);
            return true;
        } catch (CommandException e) {
            message.sendMsg(sender, e.getLocalizedMessage());
        } catch (Throwable t) {
            message.error("An error occurred while executing the command", t);
        }
        return true;
    }

    @Nullable
    private List<String> onTabComplete0(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String alias, @NotNull String[] args) {
        try {
            if (args[0].equals("search") && sender.hasPermission("bauc.search")) {
                String last = args[args.length - 1].toLowerCase();
                if (last.isEmpty()) return List.of(Lang.getMessage("start_entering_item_name"));
                return trieManager.getTrie().getAllKeysWithPrefix(last);
            }
            return command.getTabCompleter(sender, args);
        } catch (Throwable t) {
            message.error("An error occurred while executing getTabCompleter. Input '%s'", t, Joiner.on(" ").join(args));
        }
        return Collections.emptyList();
    }

    @CanIgnoreReturnValue
    public File saveIfNotExist(String path) {
        path = path.replace('\\', '/');
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        var f = new File(getDataFolder(), path);
        if (!f.exists()) {
            saveResource(path, false);
        }
        return f;
    }

    public static String getServerId() {
        return dbCfg.getServerId();
    }

    public static Set<String> getBlackList() {
        return blackList;
    }

    public Command<CommandSender> getCommand() {
        return command;
    }

    public static TrieManager getTrieManager() {
        return trieManager;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public static EventManager getEventManager() {
        return eventManager;
    }
}
