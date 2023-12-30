package org.by1337.bauction;

import com.google.common.base.Charsets;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.by1337.api.BLib;
import org.by1337.api.chat.util.Message;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.command.argument.*;
import org.by1337.api.command.requires.RequiresPermission;
import org.by1337.api.configuration.YamlConfig;
import org.by1337.api.configuration.adapter.AdapterRegistry;
import org.by1337.api.configuration.adapter.impl.primitive.AdapterEnum;
import org.by1337.api.util.NameKey;
import org.by1337.bauction.auc.User;
import org.by1337.bauction.boost.Boost;
import org.by1337.bauction.command.argument.ArgumentFullOrCount;
import org.by1337.bauction.config.Config;
import org.by1337.bauction.config.adapter.*;
import org.by1337.bauction.datafix.UpdateManager;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.db.kernel.FileDataBase;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.impl.CallBack;
import org.by1337.bauction.menu.impl.ConfirmMenu;
import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.db.event.SellItemEvent;
import org.by1337.bauction.menu.impl.PlayerItemsView;
import org.by1337.bauction.menu.requirement.IRequirement;
import org.by1337.bauction.menu.requirement.Requirements;
import org.by1337.bauction.network.PacketConnection;
import org.by1337.bauction.network.PacketType;
import org.by1337.bauction.network.in.PlayInPingResponsePacket;
import org.by1337.bauction.network.out.PlayOutPingRequestPacket;
import org.by1337.bauction.placeholder.PlaceholderHook;
import org.by1337.bauction.search.TrieManager;
import org.by1337.bauction.test.FakePlayer;
import org.by1337.bauction.util.*;
import org.by1337.bauction.util.TagUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public final class Main extends JavaPlugin {
    private static Message message;
    private static Plugin instance;
    private static Config cfg;
    private static FileDataBase storage;
    private Command<CommandSender> command;
    private static Economy econ;
    private TrieManager trieManager;
    private static TimeUtil timeUtil;
    private PlaceholderHook placeholderHook;
    private static UniqueNameGenerator uniqueNameGenerator;
    private static YamlConfig dbCfg;
    private boolean loaded;
    private static Set<String> blackList = new HashSet<>();
    private static String serverId;

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
        int seed = dbCfg.getContext().getAsInteger("name-generator.last-seed");
        serverId = dbCfg.getContext().getAsString("server-id");
        uniqueNameGenerator = new UniqueNameGenerator(seed);
        dbCfg.getContext().set("name-generator.last-seed", seed + 1);
        dbCfg.trySave();

        registerAdapters();
        UpdateManager.checkUpdate();
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp.getProvider();
        Lang.load(this);
        cfg = new Config(this);
        timeUtil = new TimeUtil();
        initCommand();
        new Metrics(this, 20300);
        trieManager = new TrieManager(this);
        TagUtil.loadAliases(this);
        placeholderHook = new PlaceholderHook();
        placeholderHook.register();
        blackList = new HashSet<>(cfg.getConfig().getList("black-list", String.class, Collections.emptyList()));
        loadDb();
    }

    private void loadDb() {
        String dbType = dbCfg.getContext().getAsString("db-type");
        if ("mysql".equals(dbType)) {
            new Thread(() -> {
                TimeCounter timeCounter = new TimeCounter();

                try {
                    storage = new MysqlDb(cfg.getCategoryMap(), cfg.getSortingMap(),
                            dbCfg.getContext().getAsString("mysql-settings.host"),
                            dbCfg.getContext().getAsString("mysql-settings.db-name"),
                            dbCfg.getContext().getAsString("mysql-settings.user"),
                            dbCfg.getContext().getAsString("mysql-settings.password"),
                            dbCfg.getContext().getAsInteger("mysql-settings.port")
                    );
                    storage.load();
                    loaded = true;
                } catch (IOException | SQLException e) {
                    message.error("failed to load db!", e);
                    instance.getServer().getPluginManager().disablePlugin(instance);
                }
                message.logger(Lang.getMessages("successful_loading"), storage.getSellItemsSize(), timeCounter.getTime());

                getCommand("bauc").setTabCompleter(this::onTabComplete0);
                getCommand("bauc").setExecutor(this::onCommand0);
            }).start();
        } else {
            if (!"file".equals(dbType)) {
                dbCfg.getContext().set("db-type", "file");
                dbCfg.trySave();
            }
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
                message.logger(Lang.getMessages("successful_loading"), storage.getSellItemsSize(), timeCounter.getTime());
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
            dbCfg = new YamlConfig(file);
            return true;
        } catch (IOException | InvalidConfigurationException e) {
            message.error(e);
        }
        return false;
    }

    public static YamlConfig getDbCfg() {
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

    public static Economy getEcon() {
        return econ;
    }

    public static TimeUtil getTimeUtil() {
        return timeUtil;
    }


    private void initCommand() {
        command = new Command<CommandSender>("bauc")
                .addSubCommand(new Command<CommandSender>("reload")
                                //<editor-fold desc="reload" defaultstate="collapsed">
                                .requires(new RequiresPermission<>("bauc.reload"))
                                .executor((sender, args) -> {
                                    TimeCounter timeCounter = new TimeCounter();

                                    getCommand("bauc").setTabCompleter(instance);
                                    getCommand("bauc").setExecutor(instance);

                                    message.sendMsg(sender, "&fUnloading db...");
                                    try {
                                        storage.save();
                                        storage.close();
                                    } catch (IOException e) {
                                        message.error("failed to save db", e);
                                    }
                                    message.sendMsg(sender, "&fReloading other files...");
                                    loadDbCfg();
                                    int seed = dbCfg.getContext().getAsInteger("name-generator.last-seed");
                                    uniqueNameGenerator = new UniqueNameGenerator(seed);
                                    dbCfg.getContext().set("name-generator.last-seed", seed + 1);
                                    dbCfg.trySave();
                                    UpdateManager.checkUpdate();
                                    Lang.load(this);
                                    cfg.reload(instance);
                                    timeUtil.reload();
                                    trieManager.reload(instance);
                                    TagUtil.loadAliases(instance);
                                    blackList = new HashSet<>(cfg.getConfig().getList("black-list", String.class, Collections.emptyList()));
                                    message.sendMsg(sender, "&fLoading db...");
                                    loadDb();

                                    message.sendMsg(sender, Lang.getMessages("plugin_reload"), timeCounter.getTime());
                                })
                        //</editor-fold>
                )
                .requires(new RequiresPermission<>("bauc.use"))
                .addSubCommand(new Command<CommandSender>("admin")
                        .requires(new RequiresPermission<>("bauc.admin"))
                        .addSubCommand(new Command<CommandSender>("debug")
                                        //<editor-fold desc="debug" defaultstate="collapsed">
                                        .requires(new RequiresPermission<>("bauc.admin.debug"))
                                        .addSubCommand(new Command<CommandSender>("ping")
                                                .requires(new RequiresPermission<>("bauc.admin.ping"))
                                                .requires((sender -> storage instanceof MysqlDb))
                                                .executor(((sender, args) -> {
                                                    new Thread(() -> {
                                                        PacketConnection connection = ((MysqlDb) storage).getPacketConnection();
                                                        if (!connection.hasConnection()){
                                                            message.sendMsg(sender, "&cHas no connection!");
                                                            return;
                                                        }
                                                        if (connection.hasListener("pingCmdListener")){
                                                            message.sendMsg(sender, "&cPls wait...");
                                                            return;
                                                        }
                                                        AtomicInteger response = new AtomicInteger();

                                                        PacketConnection.Listener<PlayInPingResponsePacket> pingResponse = packet -> {
                                                            if (packet.getTo().equals(serverId)){
                                                                message.sendMsg(sender, "&aPing '%s' %s ms.", packet.getFrom(), packet.getPing());
                                                                response.getAndIncrement();
                                                                return null;
                                                            }
                                                            return packet;
                                                        };
                                                        connection.register("pingCmdListener", PacketType.PING_RESPONSE, pingResponse);
                                                        int lost = 0;
                                                        for (int i = 1; i <= 5; i++) {
                                                            int last = response.get();
                                                            message.sendMsg(sender, "&7Start pinging... try %s", i);
                                                            PlayOutPingRequestPacket packet = new PlayOutPingRequestPacket(serverId);
                                                            connection.saveSend(packet);
                                                            try {
                                                                Thread.sleep(2000);
                                                            }catch (Exception e){
                                                            }
                                                            if (last - response.get() == 0){
                                                                message.sendMsg(sender, "&cLost all");
                                                                lost++;
                                                            }
                                                        }

                                                        connection.unregister("pingCmdListener", PacketType.PING_RESPONSE);
                                                        message.sendMsg(sender, "&7finish. Lost %s", lost);
                                                    }).start();
                                                }))
                                        )
                                        .addSubCommand(new Command<CommandSender>("clear")
                                                .requires(new RequiresPermission<>("bauc.admin.debug.clear"))
                                                .requires(s -> !(storage instanceof MysqlDb))
                                                .executor(((sender, args) -> {
                                                    if (!(sender instanceof Player player))
                                                        throw new CommandException(Lang.getMessages("must_be_player"));
                                                    CallBack<Optional<ConfirmMenu.Result>> callBack = (res) -> {
                                                        if (res.isPresent()) {
                                                            if (res.get() == ConfirmMenu.Result.ACCEPT) {
                                                                storage.clear();
                                                                message.sendMsg(player, Lang.getMessages("auc-cleared"));
                                                            }
                                                        }
                                                        player.closeInventory();
                                                    };
                                                    ItemStack itemStack = new ItemStack(Material.JIGSAW);
                                                    ItemMeta im = itemStack.getItemMeta();
                                                    im.setDisplayName(message.messageBuilder(Lang.getMessages("auc-clear-confirm")));
                                                    itemStack.setItemMeta(im);
                                                    ConfirmMenu menu = new ConfirmMenu(callBack, itemStack, player);
                                                    menu.open();
                                                }))
                                        )
                                        .addSubCommand(new Command<CommandSender>("stress")
                                                .requires(new RequiresPermission<>("bauc.admin.debug.stress"))
                                                .argument(new ArgumentIntegerAllowedMatch<>("count", List.of("[count]")))
                                                .argument(new ArgumentIntegerAllowedMatch<>("repeat", List.of("[repeat]")))
                                                .argument(new ArgumentIntegerAllowedMatch<>("cd", List.of("[cd]")))
                                                .argument(new ArgumentIntegerAllowedMatch<>("limit", List.of("[limit]")))
                                                .executor((sender, args) -> {
                                                    int count = (int) args.getOrDefault("count", 1);
                                                    int repeat = (int) args.getOrDefault("repeat", 1);
                                                    int cd = (int) args.getOrDefault("cd", 1);
                                                    int limit = (int) args.getOrDefault("limit", Integer.MAX_VALUE);
                                                    TimeCounter timeCounter = new TimeCounter();
                                                    FakePlayer fakePlayer = new FakePlayer(storage, limit);
                                                    new BukkitRunnable() {
                                                        int x = 0;
                                                        List<Long> list = new ArrayList<>();

                                                        @Override
                                                        public void run() {
                                                            timeCounter.reset();
                                                            x++;
                                                            for (int i = 0; i < count; i++) {
                                                                fakePlayer.randomAction();
                                                            }
                                                            long time = timeCounter.getTime();
                                                            message.sendMsg(sender, "Completed in %s ms. %s", time, x);
                                                            list.add(time);
                                                            if (x >= repeat) {
                                                                long l = 0;
                                                                for (Long l1 : list) {
                                                                    l += l1;
                                                                }
                                                                l /= list.size();

                                                                String s = String.format("Deals per second: %s. Average duration: %s ms. Total deals made: %s. Items on auction: %s.",
                                                                        count * (20 / cd),
                                                                        l,
                                                                        count * repeat,
                                                                        storage.getSellItemsSize()
                                                                );
                                                                message.logger(s);
                                                                message.sendMsg(sender, s);
                                                                cancel();
                                                            }
                                                        }
                                                    }.runTaskTimerAsynchronously(instance, 0, cd);
                                                })
                                        )
                                //</editor-fold>
                        )

                        .addSubCommand(new Command<CommandSender>("addTag")
                                        //<editor-fold desc="addTag" defaultstate="collapsed">
                                        .requires(new RequiresPermission<>("bauc.admin.addTag"))
                                        .argument(new ArgumentString<>("key", List.of("[tag key]")))
                                        .argument(new ArgumentString<>("value", List.of("[tag value]")))
                                        .executor((sender, args) -> {
                                                    String key = (String) args.getOrThrow("key");
                                                    String value = (String) args.getOrThrow("value");

                                                    if (!(sender instanceof Player player))
                                                        throw new CommandException(Lang.getMessages("must_be_player"));
                                                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                                                    if (itemStack.getType().isAir()) {
                                                        throw new CommandException(Lang.getMessages("item_in_hand_required"));
                                                    }
                                                    ItemMeta im = itemStack.getItemMeta();
                                                    im.getPersistentDataContainer().set(NamespacedKey.fromString(key), PersistentDataType.STRING, value);
                                                    itemStack.setItemMeta(im);
                                                    message.sendMsg(sender, "&adone");
                                                }
                                        )
                                //</editor-fold>
                        )
                        .addSubCommand(new Command<CommandSender>("open")
                                        //<editor-fold desc="open" defaultstate="collapsed">
                                        .requires(new RequiresPermission<>("bauc.admin.open"))
                                        .argument(new ArgumentPlayer<>("player"))
                                        .argument(new ArgumentSetList<>("category", cfg.getCategoryMap().keySet().stream().map(NameKey::getName).toList()))
                                        .executor((sender, args) -> {
                                            Player player = (Player) args.getOrThrow("player");
                                            String categoryS = (String) args.getOrThrow("category");

                                            Category category = cfg.getCategoryMap().get(new NameKey(categoryS, true));

                                            if (category == null) {
                                                message.sendMsg(sender, "unknown category %s", categoryS);
                                                return;
                                            }

                                            User user = storage.getUserOrCreate(player);
                                            MainMenu menu = new MainMenu(user, player);

                                            int index = menu.getCategories().indexOf(category);

                                            if (index == -1) {
                                                message.sendMsg(sender, "unknown category %s", categoryS);
                                                menu.close();
                                                return;
                                            }
                                            menu.getCategories().current = index;
                                            menu.open();
                                        })
                                //</editor-fold>
                        )
                        .addSubCommand(new Command<CommandSender>("parse")
                                //<editor-fold desc="parse" defaultstate="collapsed">
                                .requires(new RequiresPermission<>("bauc.parse"))
                                .addSubCommand(new Command<CommandSender>("tags")
                                                .requires(new RequiresPermission<>("bauc.parse.tags"))
                                                .executor((sender, args) -> {
                                                    if (!(sender instanceof Player player))
                                                        throw new CommandException(Lang.getMessages("must_be_player"));
                                                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                                                    if (itemStack.getType().isAir()) {
                                                        throw new CommandException(Lang.getMessages("item_in_hand_required"));
                                                    }
                                                    message.sendMsg(sender, TagUtil.getTags(itemStack).toString());
                                                })
                                        //</editor-fold>
                                )
                                .addSubCommand(new Command<CommandSender>("nbt")
                                                //<editor-fold desc="nbt" defaultstate="collapsed">
                                                .requires(new RequiresPermission<>("bauc.parse.nbt"))
                                                .executor((sender, args) -> {
                                                    if (!(sender instanceof Player player))
                                                        throw new CommandException(Lang.getMessages("must_be_player"));
                                                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                                                    if (itemStack.getType().isAir()) {
                                                        throw new CommandException(Lang.getMessages("item_in_hand_required"));
                                                    }
                                                    message.sendMsg(sender, new String(Base64.getDecoder().decode(BLib.getApi().getItemStackSerialize().serialize(itemStack))));
                                                })
                                        //</editor-fold>
                                )
                        )
                        .addSubCommand(new Command<CommandSender>("push")
                                        //<editor-fold desc="push" defaultstate="collapsed">
                                        .requires(new RequiresPermission<>("bauc.admin.push"))
                                        .argument(new ArgumentIntegerAllowedMatch<>("price", List.of(Lang.getMessages("price_tag"))))
                                        .argument(new ArgumentInteger<>("amount", List.of(Lang.getMessages("quantity_tag"))))
                                        .argument(new ArgumentString<>("time", List.of(Lang.getMessages("sale_time_tag"))))
                                        .executor((sender, args) -> {
                                                    int amount = (int) args.getOrDefault("amount", 1);
                                                    int price = (int) args.getOrThrow("price", Lang.getMessages("price_not_specified"));
                                                    if (!(sender instanceof Player player))
                                                        throw new CommandException(Lang.getMessages("must_be_player"));

                                                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                                                    if (itemStack.getType().isAir()) {
                                                        throw new CommandException(Lang.getMessages("cannot_trade_air"));
                                                    }
                                                    TimeCounter timeCounter = new TimeCounter();
                                                    Random random = new Random();
                                                    User user = storage.getUserOrCreate(player);
                                                    long time = NumberUtil.getTime(((String) args.getOrDefault("time", "2d")));
                                                    for (int i = 0; i < amount; i++) {
                                                        CSellItem sellItem = new CSellItem(player, itemStack, price + random.nextInt(price / 2), time);
                                                        SellItemEvent event = new SellItemEvent(user, sellItem);
                                                        storage.validateAndAddItem(event);
                                                        if (!event.isValid()) {
                                                            message.sendMsg(player, String.valueOf(event.getReason()));
                                                            break;
                                                        }
                                                    }
                                                    message.sendMsg(player, Lang.getMessages("successful_listing"), amount, timeCounter.getTime());
                                                }
                                        )
                                //</editor-fold>
                        )
                )
                .addSubCommand(new Command<CommandSender>("sell")
                                //<editor-fold desc="sell" defaultstate="collapsed">
                                .requires(new RequiresPermission<>("bauc.sell"))
                                .argument(new ArgumentIntegerAllowedMatch<>("price", List.of(Lang.getMessages("price_tag")),
                                        cfg.getConfig().getAsInteger("offer-min-price", 1),
                                        cfg.getConfig().getAsInteger("offer-max-price", Integer.MAX_VALUE)
                                ))
                                .argument(new ArgumentFullOrCount("arg"))
                                .executor(((sender, args) -> {
                                    if (!(sender instanceof Player player))
                                        throw new CommandException(Lang.getMessages("must_be_player"));
                                    int price = (int) args.getOrThrow("price", Lang.getMessages("price_not_specified"));

                                    String saleByThePieceS = String.valueOf(args.getOrDefault("arg", "full:false"));

                                    boolean saleByThePiece = !(saleByThePieceS.equals("full"));


                                    int amount = -1; //(int) args.getOrDefault("amount", -1);
                                    if (!saleByThePieceS.startsWith("f")) {
                                        try {
                                            amount = Integer.parseInt(saleByThePieceS);
                                        } catch (NumberFormatException e) {
                                            message.sendMsg(sender, "&cУкажите число для продажи определённого количества предметов!");
                                            return;
                                        }
                                    }

                                    ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
                                    if (itemStack.getType().isAir()) {
                                        throw new CommandException(Lang.getMessages("cannot_trade_air"));
                                    }

                                    int cashback = 0;
                                    if (amount != -1) {
                                        if (itemStack.getAmount() > amount) {
                                            cashback = itemStack.getAmount() - amount;
                                            itemStack.setAmount(amount);
                                        }
                                    }

                                    User user = storage.getUserOrCreate(player);
                                    CSellItem sellItem = new CSellItem(player, itemStack, price, cfg.getDefaultSellTime() + user.getExternalSellTime(), saleByThePiece);

                                    for (String tag : sellItem.getTags()) {
                                        if (blackList.contains(tag)) {
                                            message.sendMsg(player, sellItem.replace(Lang.getMessages("item-in-black-list")));
                                            return;
                                        }
                                    }

                                    SellItemEvent event = new SellItemEvent(user, sellItem);
                                    storage.validateAndAddItem(event);
                                    if (event.isValid()) {
                                        player.getInventory().getItemInMainHand().setAmount(cashback);
                                        message.sendMsg(player, sellItem.replace(Lang.getMessages("successful_single_listing")));
                                    } else {
                                        message.sendMsg(player, String.valueOf(event.getReason()));
                                    }
                                }))
                        //</editor-fold>
                )
                .addSubCommand(new Command<CommandSender>("search")
                                //<editor-fold desc="search" defaultstate="collapsed">
                                .requires(new RequiresPermission<>("bauc.search"))
                                .argument(new ArgumentStrings<>("tags"))
                                .executor((sender, args) -> {
                                    if (!(sender instanceof Player player))
                                        throw new CommandException(Lang.getMessages("must_be_player"));

                                    String[] rawtags = ((String) args.getOrThrow("tags", Lang.getMessages("tags_required"))).split(" ");
                                    List<String> tags = new ArrayList<>();
                                    for (String rawtag : rawtags) {
                                        tags.addAll(trieManager.getTrie().getAllWithPrefix(rawtag));
                                    }
                                    Category custom = cfg.getSorting().getAs("special.search", Category.class);
                                    custom.setTags(new HashSet<>(tags));

                                    User user = storage.getUserOrCreate(player);

                                    MainMenu menu = new MainMenu(user, player);
                                    menu.setCustomCategory(custom);
                                    menu.open();
                                })
                        //</editor-fold>
                )
                .addSubCommand(new Command<CommandSender>("view")
                                //<editor-fold desc="view" defaultstate="collapsed">
                                .requires(new RequiresPermission<>("bauc.view"))
                                .argument(new ArgumentString<>("player", () -> List.of(Bukkit.getOnlinePlayers().stream().map(Player::getName).toArray(String[]::new))))
                                .executor(((sender, args) -> {
                                    if (!(sender instanceof Player senderP))
                                        throw new CommandException(Lang.getMessages("must_be_player"));

                                    String player = (String) args.get("player");
                                    if (player == null) {
                                        message.sendMsg(sender, Lang.getMessages("player-not-selected"));
                                        return;
                                    }
                                    UUID uuid;
                                    if (UUIDUtils.isUUID(player)) {
                                        uuid = UUID.fromString(player);
                                    } else {
                                        Player p = Bukkit.getPlayer(player);
                                        if (p != null) {
                                            uuid = p.getUniqueId();
                                        } else {
                                            uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + player).getBytes(Charsets.UTF_8));
                                        }
                                    }
                                    if (!storage.hasUser(uuid)) {
                                        message.sendMsg(sender, Lang.getMessages("player-not-found"));
                                        return;
                                    }
                                    User user = storage.getUserOrCreate(senderP);
                                    PlayerItemsView menu = new PlayerItemsView(user, senderP, uuid, player);
                                    menu.open();
                                }))
                        //</editor-fold>
                )
                .executor(((sender, args) -> {
                    if (!(sender instanceof Player player))
                        throw new CommandException(Lang.getMessages("must_be_player"));
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
            String last = args[args.length - 1];
            if (last.isEmpty()) return List.of(Lang.getMessages("start_entering_item_name"));
            return trieManager.getTrie().getAllKeysWithPrefix(last);
        }
        return command.getTabCompleter(sender, args);
    }

    public static String getServerId() {
        return serverId;
    }
}
