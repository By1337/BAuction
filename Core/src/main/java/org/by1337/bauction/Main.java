package org.by1337.bauction;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.boost.Boost;
import org.by1337.bauction.command.*;
import org.by1337.bauction.config.Config;
import org.by1337.bauction.config.adapter.*;
import org.by1337.bauction.datafix.UpdateManager;
import org.by1337.bauction.db.kernel.FileDataBase;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.hook.EconomyHook;
import org.by1337.bauction.hook.impl.PlayerPointsHook;
import org.by1337.bauction.hook.impl.VaultHook;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.menu.requirement.IRequirement;
import org.by1337.bauction.menu.requirement.Requirements;
import org.by1337.bauction.placeholder.PlaceholderHook;
import org.by1337.bauction.search.TrieManager;
import org.by1337.bauction.util.*;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.configuration.YamlConfig;
import org.by1337.blib.configuration.adapter.AdapterRegistry;
import org.by1337.blib.configuration.adapter.impl.primitive.AdapterEnum;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public final class Main extends JavaPlugin {
    private static Message message;
    private static Plugin instance;
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

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        message = new Message(getLogger());
        if (!loadDbCfg()) {
            message.error("failed to load dbCfg.yml!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getCommand("bauc").setPermission("bauc.use");
        registerAdapters();
        loadSeed();
        Lang.load(this);
        cfg = new Config(this);
        timeUtil = new TimeUtil();
        trieManager = new TrieManager(this);
        TagUtil.loadAliases(this);
        UpdateManager.checkUpdate();
        dbCfg.validate();

        String econType = Objects.requireNonNull(cfg.getConfig().getAsString("economy"), "тип экономики не указан!");
        if (econType.equalsIgnoreCase("vault")) {
            econ = new VaultHook();
        } else if (econType.equalsIgnoreCase("playerpoints")) {
            econ = new PlayerPointsHook();
        } else {
            throw new IllegalStateException("Параметр economy имеет не правильное значение! '" + econType + "'. Ожидалось 'Vault' | 'PlayerPoints'");
        }

        initCommand();
        new Metrics(this, 20300);
        placeholderHook = new PlaceholderHook();
        placeholderHook.register();
        blackList = new HashSet<>(cfg.getConfig().getList("black-list", String.class, Collections.emptyList()));
        loadDb();
        new VersionChecker();
    }

    public void loadDb() {
        if (storage != null) {
            throw new IllegalStateException("data base already loaded!");
        }

        if (dbCfg.getDbType() == DbCfg.DbType.MYSQL) {
            new Thread(() -> {
                TimeCounter timeCounter = new TimeCounter();

                try {
                    storage = new MysqlDb(cfg.getCategoryMap(), cfg.getSortingMap(),
                            dbCfg.getHost(),
                            dbCfg.getDbName(),
                            dbCfg.getUser(),
                            dbCfg.getPassword(),
                            dbCfg.getPort()
                    );
                    storage.load();
                    loaded = true;
                } catch (IOException | SQLException e) {
                    message.error("failed to load db!", e);
                    instance.getServer().getPluginManager().disablePlugin(instance);
                }
                message.logger(Lang.getMessage("successful_loading"), storage.getSellItemsSize(), timeCounter.getTime());

                getCommand("bauc").setTabCompleter(this::onTabComplete0);
                getCommand("bauc").setExecutor(this::onCommand0);
            }).start();
        } else {
            new Thread(() -> {
                TimeCounter timeCounter = new TimeCounter();
                storage = new FileDataBase(cfg.getCategoryMap(), cfg.getSortingMap());
                try {
                    storage.load();
                    loaded = true;
                } catch (IOException e) {
                    message.error("failed to load db!", e);
                    instance.getServer().getPluginManager().disablePlugin(instance);
                }
                message.logger(Lang.getMessage("successful_loading"), storage.getSellItemsSize(), timeCounter.getTime());
                getCommand("bauc").setTabCompleter(this::onTabComplete0);
                getCommand("bauc").setExecutor(this::onCommand0);
            }).start();
        }
    }

    @Override
    public void onDisable() {
        if (!loaded) return;
        try {
            storage.save();
            storage.close();
        } catch (IOException e) {
            message.error("failed to save db", e);
        }
        AdapterRegistry.unregisterPrimitiveAdapter(Sorting.SortingType.class);
        AdapterRegistry.unregisterPrimitiveAdapter(InventoryType.class);
        AdapterRegistry.unregisterAdapter(Sorting.class);
        AdapterRegistry.unregisterAdapter(Category.class);
        AdapterRegistry.unregisterAdapter(Requirements.class);
        AdapterRegistry.unregisterAdapter(CustomItemStack.class);
        AdapterRegistry.unregisterAdapter(Boost.class);
        AdapterRegistry.unregisterAdapter(IRequirement.class);
        placeholderHook.unregister();
    }

    private void registerAdapters() {
        AdapterRegistry.registerPrimitiveAdapter(Sorting.SortingType.class, new AdapterEnum<>(Sorting.SortingType.class));
        AdapterRegistry.registerPrimitiveAdapter(InventoryType.class, new AdapterEnum<>(InventoryType.class));
        AdapterRegistry.registerAdapter(Sorting.class, new AdapterSortingType());
        AdapterRegistry.registerAdapter(Category.class, new AdapterCategory());
        AdapterRegistry.registerAdapter(Requirements.class, new AdapterRequirements());
        AdapterRegistry.registerAdapter(CustomItemStack.class, new AdapterCustomItemStack());
        AdapterRegistry.registerAdapter(Boost.class, new AdapterBoost());
        AdapterRegistry.registerAdapter(IRequirement.class, new AdapterIRequirement());
    }

    private boolean loadDbCfg() {
        try {
            File file = new File(getDataFolder() + "/dbCfg.yml");
            if (!file.exists()) {
                saveResource("dbCfg.yml", false);
            }
            dbCfg = new DbCfg(new YamlConfig(file));
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            message.error(e);
        }
        return false;
    }

    public void reloadDbCfg() {
        loadDbCfg();
        loadSeed();
    }

    private void loadSeed() {
        int seed = dbCfg.getLastSeed();
        uniqueNameGenerator = new UniqueNameGenerator(seed);
        dbCfg.getContext().set("name-generator.last-seed", seed + 1);
        if (dbCfg.getContext() instanceof YamlConfig yamlConfig) {
            yamlConfig.trySave();
        }
    }

    public static DbCfg getDbCfg() {
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

    public void reloadConfigs() {
        if (storage != null) {
            throw new IllegalStateException("pls unload data base!");
        }
        reloadDbCfg();
        Lang.load(this);
        cfg.reload(instance);
        timeUtil.reload();
        trieManager.reload(instance);
        TagUtil.loadAliases(instance);
        blackList = new HashSet<>(Main.getCfg().getConfig().getList("black-list", String.class, Collections.emptyList()));
    }

    public void unloadDb() {
        try {
            storage.save();
            storage.close();
            storage = null;
        } catch (IOException e) {
            message.error("failed to save db", e);
        }
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
                        .addSubCommand(new OpenCmd("open"))
                        .addSubCommand(new Command<CommandSender>("parse")
                                .requires(new RequiresPermission<>("bauc.parse"))
                                .addSubCommand(new ParseTagsCmd("tags"))
                                .addSubCommand(new ParseNbtCmd("nbt"))
                        )
                )
                .addSubCommand(new SellCmd("sell"))
                .addSubCommand(new SearchCmd("search"))
                .addSubCommand(new ViewCommand("view"))
                .executor(((sender, args) -> {
                    if (!(sender instanceof Player player))
                        throw new CommandException(Lang.getMessage("must_be_player"));
                    User user = storage.getUserOrCreate(player);
                    MainMenu menu = new MainMenu(user, player);
                    menu.open();
                }))
        ;
    }


    private boolean onCommand0(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
        try {
            command.process(sender, args);
            return true;
        } catch (CommandException e) {
            message.sendMsg(sender, e.getLocalizedMessage());
        }
        return true;
    }

    @Nullable
    private List<String> onTabComplete0(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args[0].equals("search") && sender.hasPermission("bauc.search")) {
            String last = args[args.length - 1].toLowerCase();
            if (last.isEmpty()) return List.of(Lang.getMessage("start_entering_item_name"));
            return trieManager.getTrie().getAllKeysWithPrefix(last);
        }
        return command.getTabCompleter(sender, args);
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
}
