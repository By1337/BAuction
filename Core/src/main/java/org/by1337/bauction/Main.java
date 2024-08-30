package org.by1337.bauction;

import com.google.common.base.Joiner;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.by1337.bauction.assets.AssetsManager;
import org.by1337.bauction.boost.Boost;
import org.by1337.bauction.command.impl.*;
import org.by1337.bauction.config.Config;
import org.by1337.bauction.config.adapter.AdapterBoost;
import org.by1337.bauction.config.adapter.AdapterCategory;
import org.by1337.bauction.config.adapter.AdapterSortingType;
import org.by1337.bauction.datafix.UpdateManager;
import org.by1337.bauction.db.kernel.FileDatabase;
import org.by1337.bauction.db.kernel.MemoryDatabase;
import org.by1337.bauction.db.kernel.module.ExpiredItemsRemover;
import org.by1337.bauction.event.EventManager;
import org.by1337.bauction.hook.econ.EconomyHook;
import org.by1337.bauction.hook.econ.impl.BVaultHook;
import org.by1337.bauction.hook.econ.impl.PlayerPointsHook;
import org.by1337.bauction.hook.econ.impl.VaultHook;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.logg.FileLogger;
import org.by1337.bauction.logg.PluginLogger;
import org.by1337.bauction.menu.*;
import org.by1337.bauction.placeholder.PlaceholderHook;
import org.by1337.bauction.search.TrieManager;
import org.by1337.bauction.util.Metrics;
import org.by1337.bauction.util.VersionChecker;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.auction.Sorting;
import org.by1337.bauction.util.auction.TagUtil;
import org.by1337.bauction.util.config.ConfigUtil;
import org.by1337.bauction.util.config.DbCfg;
import org.by1337.bauction.util.id.UniqueIdGenerator;
import org.by1337.bauction.util.plugin.PluginEnablePipeline;
import org.by1337.bauction.util.time.TimeCounter;
import org.by1337.bauction.util.time.TimeUtil;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.configuration.adapter.AdapterRegistry;
import org.by1337.blib.configuration.adapter.impl.primitive.AdapterEnum;
import org.by1337.bmenu.BMenuApi;
import org.by1337.bmenu.menu.MenuLoader;
import org.by1337.bmenu.menu.MenuProviderRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

public final class Main extends JavaPlugin {
    public static boolean RUNNING_IN_IDE = false;
    public static final boolean IS_RELEASE = false;
    public static boolean DEBUG_MODE = true;
    private static final String DEBUG_LANG = "en";
    private Message message;
    private static Main instance;
    private Config cfg;
    private MemoryDatabase storage;
    private Command<CommandSender> command;
    private EconomyHook econ;
    private TrieManager trieManager;
    private TimeUtil timeUtil;
    private PlaceholderHook placeholderHook;
    private UniqueIdGenerator uniqueIdGenerator;
    private DbCfg dbCfg;
    private Set<String> blackList = new HashSet<>();
    private EventManager eventManager;
    private FileLogger fileLogger;
    private MenuLoader menuLoader;
    private PluginEnablePipeline enablePipeline;
    private Metrics metrics;
    private PluginLogger pluginLogger;
    private AssetsManager assetsManager;

    @VisibleForTesting
    protected Main(JavaPluginLoader loader, PluginDescriptionFile description, File dataFolder, File file) {
        super(loader, description, dataFolder, file);
    }

    public Main() {
    }

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
            menuLoader = new MenuLoader(this, new File(getDataFolder(), "menu"),
                    DEBUG_MODE || RUNNING_IN_IDE ? MenuLoader.ResourceLeakDetectorMode.PANIC : MenuLoader.ResourceLeakDetectorMode.DEFAULT
            );
            menuLoader.load();
        });
        enablePipeline.enable("load db cfg", () -> {
            dbCfg = new DbCfg(ConfigUtil.load("dbCfg.yml"));
            dbCfg.validate();
        });
        enablePipeline.enable("registerAdapters", this::registerAdapters);

        enablePipeline.enable("load UniqueNameGenerator", () -> {
            File file = new File(getDataFolder(), "uidLastPos");
            if (!file.exists()) {
                uniqueIdGenerator = new UniqueIdGenerator(0);
            } else {
                String data = Files.readString(file.toPath());
                uniqueIdGenerator = new UniqueIdGenerator(Long.parseLong(data));
            }
        });

        enablePipeline.enable("load cfg", () -> {
            cfg = new Config();
        });
        enablePipeline.enable("load assetsManager", () -> {
            assetsManager = new AssetsManager(this, cfg.getLang());
        });
        enablePipeline.enable("load lang", () -> {
            Lang.load(this, assetsManager);
        });
        enablePipeline.enable("load logger", () -> {
            if (cfg.isLogging()) {
                fileLogger = new FileLogger(new File(getDataFolder(), "logs"), this);
            }
        });
        enablePipeline.enable("load event manager", () -> {
            eventManager = new EventManager(ConfigUtil.load("listener.yml"));
        });
        enablePipeline.enable("load time util", () -> {
            timeUtil = new TimeUtil();
        });
        enablePipeline.enable("load TrieManager", () -> {
            trieManager = new TrieManager(this, assetsManager);
            TagUtil.loadAliases(this);
        });
        enablePipeline.enable("load econ", () -> {
            String econType = Objects.requireNonNull(cfg.getConfig().getAsString("economy"), "economy type not specified!");
            if (econType.equalsIgnoreCase("vault")) {
                econ = new VaultHook();
            } else if (econType.equalsIgnoreCase("playerpoints")) {
                econ = new PlayerPointsHook();
            } else if (econType.equalsIgnoreCase("bvault")) {
                econ = new BVaultHook(cfg.getConfig());
            } else {
                throw new IllegalStateException("The economy parameter has the wrong value! '" + econType + "'. Expected 'Vault' | 'PlayerPoints' | 'BVault'");
            }
        });
//        enablePipeline.enable("load metrics", () -> {
//            metrics = new Metrics(this, 20300);
//        });
        enablePipeline.enable("load PAPI hook", p -> !RUNNING_IN_IDE, () -> {
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
                storage.close();
            } catch (Throwable t) {
                message.error("failed to save db", t);
            } finally {
                storage = null;
            }
        });
        enablePipeline.disable("disable UniqueNameGenerator", p -> p.isEnabled("load UniqueNameGenerator"), () -> {
            File file = new File(getDataFolder(), "uidLastPos");
            Files.writeString(file.toPath(), String.valueOf(uniqueIdGenerator.getPos()), StandardCharsets.UTF_8);
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
            instance.message.log("[DEBUG] " + s, objects);
        } else {
            instance.pluginLogger.log(String.format("[DEBUG] " + s, objects));
        }
    }

    public void loadDb() {
        if (storage != null) {
            throw new IllegalStateException("data base already loaded!");
        }

        if ("true".equals(System.getProperty("bauction.db.memory"))) {
            debug("using memory database");
            storage = new MemoryDatabase(cfg.getCategoryMap(), cfg.getSortingMap(),
                    List.of(
                            new ExpiredItemsRemover()
                    )
            );
            storage.load();
            message.log(Lang.getMessage("successful_loading"), 0, 0);
            getCommand("bauc").setTabCompleter(this::onTabComplete0);
            getCommand("bauc").setExecutor(this::onCommand0);
        } else if (dbCfg.getDbType() == DbCfg.DbType.MYSQL && false) {

        } else {
            TimeCounter timeCounter = new TimeCounter();

            storage = new FileDatabase(cfg.getCategoryMap(), cfg.getSortingMap(),
                    List.of(
                            new ExpiredItemsRemover()
                    )
            );
            storage.load();
            message.log(Lang.getMessage("successful_loading"), storage.getSellItemsCount(), timeCounter.getTime());
            getCommand("bauc").setTabCompleter(this::onTabComplete0);
            getCommand("bauc").setExecutor(this::onCommand0);
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

    public static UniqueIdGenerator getUniqueIdGenerator() {
        return instance.uniqueIdGenerator;
    }

    public static Message getMessage() {
        return instance.message;
    }

    public static Plugin getInstance() {
        return instance;
    }

    public static Config getCfg() {
        return instance.cfg;
    }

    public static MemoryDatabase getStorage() {
        return instance.storage;
    }

    public static EconomyHook getEcon() {
        return instance.econ;
    }

    public static TimeUtil getTimeUtil() {
        return instance.timeUtil;
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
                                // .addSubCommand(new PingCmd("ping")) // todo
                                //  .addSubCommand(new ClearCmd("clear")) // todo
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
                if (last.isEmpty()) {
                    if (args.length > 2) {
                        if (Arrays.stream(args).noneMatch(s -> s.equals(Lang.getMessage("search-type")))) {
                            return Collections.singletonList(Lang.getMessage("search-type"));
                        }
                    }
                    return List.of(Lang.getMessage("start_entering_item_name"));
                }
                return trieManager.getTrie().getAllKeysWithPrefix(last);
            }
            return command.getTabCompleter(sender, args);
        } catch (Throwable t) {
            message.error("An error occurred while executing getTabCompleter. Input '%s'", t, Joiner.on(" ").join(args));
        }
        return Collections.emptyList();
    }

    @Override
    public @Nullable InputStream getResource(@NotNull String filename) {
        return RUNNING_IN_IDE ? super.getResource(DEBUG_LANG + "/" + filename) : super.getResource(filename);
    }

    public static String getServerId() {
        return instance.dbCfg == null ? "unknown" : instance.dbCfg.getServerId();
    }

    public static Set<String> getBlackList() {
        return instance.blackList;
    }

    public Command<CommandSender> getCommand() {
        return command;
    }

    public static TrieManager getTrieManager() {
        return instance.trieManager;
    }

    public PlaceholderHook getPlaceholderHook() {
        return placeholderHook;
    }

    public static EventManager getEventManager() {
        return instance.eventManager;
    }

    @VisibleForTesting
    static void setInstance(Main instance) {
        Main.instance = instance;
    }
}
