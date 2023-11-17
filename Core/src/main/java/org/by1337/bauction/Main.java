package org.by1337.bauction;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.api.BLib;
import org.by1337.api.chat.util.Message;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.command.argument.*;
import org.by1337.api.command.requires.RequiresPermission;
import org.by1337.api.configuration.adapter.AdapterRegistry;
import org.by1337.api.configuration.adapter.impl.primitive.AdapterEnum;
import org.by1337.bauction.booost.Boost;
import org.by1337.bauction.config.Config;
import org.by1337.bauction.config.adapter.*;
import org.by1337.bauction.db.MemorySellItem;
import org.by1337.bauction.db.MemoryUser;
import org.by1337.bauction.db.DataBase;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.db.event.SellItemEvent;
import org.by1337.bauction.menu.requirement.IRequirement;
import org.by1337.bauction.menu.requirement.Requirements;
import org.by1337.bauction.search.TrieManager;
import org.by1337.bauction.util.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class Main extends JavaPlugin {
    private static Message message;
    private static Plugin instance;
    private static Config cfg;
    private static DataBase storage;
    private Command command;
    private static Economy econ;
    private TrieManager trieManager;
    private static TimeUtil timeUtil;

    // todo /ah reload
    // todo /ah admin open <категория>
    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        message = new Message(getLogger());
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        econ = rsp.getProvider();


        AdapterRegistry.registerPrimitiveAdapter(Sorting.SortingType.class, new AdapterEnum<>(Sorting.SortingType.class));
        AdapterRegistry.registerPrimitiveAdapter(ItemFlag.class, new AdapterEnum<>(ItemFlag.class));
        AdapterRegistry.registerPrimitiveAdapter(InventoryType.class, new AdapterEnum<>(InventoryType.class));
        AdapterRegistry.registerAdapter(Sorting.class, new AdapterSortingType());
        AdapterRegistry.registerAdapter(Category.class, new AdapterCategory());
        AdapterRegistry.registerAdapter(Requirements.class, new AdapterRequirements());
        AdapterRegistry.registerAdapter(CustomItemStack.class, new AdapterCustomItemStack());
        AdapterRegistry.registerAdapter(Boost.class, new AdapterBoost());
        AdapterRegistry.registerAdapter(IRequirement.class, new AdapterIRequirement());

        cfg = new Config(this);
        timeUtil = new TimeUtil();

        new Thread(() -> {
            TimeCounter timeCounter = new TimeCounter();
            storage = new DataBase(cfg.getCategoryMap(), cfg.getSortingMap());
            message.logger("Успешно загружено %s предметов за %s мс.", storage.getItemsSize(), timeCounter.getTime());
            getCommand("bauc").setTabCompleter(this::onTabComplete0);
            getCommand("bauc").setExecutor(this::onCommand0);
        }).start();

        initCommand();

        new Metrics(this, 20300);
        trieManager = new TrieManager(this);
        TagUtil.loadAliases(this);
    }

    @Override
    public void onDisable() {
        storage.save();
        AdapterRegistry.unregisterPrimitiveAdapter(Sorting.SortingType.class);
        AdapterRegistry.unregisterPrimitiveAdapter(ItemFlag.class);
        AdapterRegistry.unregisterPrimitiveAdapter(InventoryType.class);
        AdapterRegistry.unregisterAdapter(Sorting.class);
        AdapterRegistry.unregisterAdapter(Category.class);
        AdapterRegistry.unregisterAdapter(Requirements.class);
        AdapterRegistry.unregisterAdapter(CustomItemStack.class);
        AdapterRegistry.unregisterAdapter(Boost.class);
        AdapterRegistry.unregisterAdapter(IRequirement.class);
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

    public static DataBase getStorage() {
        return storage;
    }

    public static Economy getEcon() {
        return econ;
    }

    public static TimeUtil getTimeUtil() {
        return timeUtil;
    }

    private void initCommand() {
        command = new Command("bauc")
                .addSubCommand(new Command("reload")
                        .requires(new RequiresPermission("bauc.reload"))
                        .executor((sender, args) -> {
                            TimeCounter timeCounter = new TimeCounter();
                            cfg = new Config(this);
                            timeUtil = new TimeUtil();
                            trieManager = new TrieManager(this);
                            TagUtil.loadAliases(this);
                            message.sendMsg(sender, "&aПлагин успешно перезагружен за %s мс.", timeCounter.getTime());
                        })
                )
                .requires(new RequiresPermission("bauc.use"))
                .addSubCommand(new Command("admin")
                        .requires(new RequiresPermission("bauc.admin"))
                        .addSubCommand(new Command("parse")
                                .requires(new RequiresPermission("bauc.parse"))
                                .addSubCommand(new Command("tags")
                                        .requires(new RequiresPermission("bauc.parse.tags"))
                                        .executor((sender, args) -> {
                                            if (!(sender instanceof Player player))
                                                throw new CommandException("Вы должны быть игроком!");
                                            ItemStack itemStack = player.getInventory().getItemInMainHand();
                                            if (itemStack.getType().isAir()) {
                                                throw new CommandException("&cУ Вас в руке должен быть предмет");
                                            }
                                            message.sendMsg(sender, TagUtil.getTags(itemStack).toString());
                                        })
                                )
                                .addSubCommand(new Command("nbt")
                                        .requires(new RequiresPermission("bauc.parse.nbt"))
                                        .executor((sender, args) -> {
                                            if (!(sender instanceof Player player))
                                                throw new CommandException("Вы должны быть игроком!");
                                            ItemStack itemStack = player.getInventory().getItemInMainHand();
                                            if (itemStack.getType().isAir()) {
                                                throw new CommandException("&cУ Вас в руке должен быть предмет");
                                            }
                                            message.sendMsg(sender, new String(Base64.getDecoder().decode(BLib.getApi().getItemStackSerialize().serialize(itemStack))));
                                        })
                                )
                        )
                        .addSubCommand(new Command("push")
                                .requires(new RequiresPermission("bauc.admin.push"))
                                .argument(new ArgumentIntegerAllowedMatch("price", List.of("[цена]")))
                                .argument(new ArgumentInteger("amount", List.of("[количество]")))
                                .argument(new ArgumentString("time", List.of("[время продажи]")))
                                .executor((sender, args) -> {
                                            int amount = (int) args.getOrDefault("amount", 1);
                                            int price = (int) args.getOrThrow("price", "&cВы должны указать цену!");
                                            if (!(sender instanceof Player player))
                                                throw new CommandException("Вы должны быть игроком!");

                                            ItemStack itemStack = player.getInventory().getItemInMainHand();
                                            if (itemStack.getType().isAir()) {
                                                throw new CommandException("&cВы не можете торговать воздухом!");
                                            }
                                            TimeCounter timeCounter = new TimeCounter();
                                            Random random = new Random();
                                            MemoryUser user = storage.getMemoryUserOrCreate(player);
                                            long time = NumberUtil.getTime(((String) args.getOrDefault("time", cfg.getDefaultSellTime() + user.getExternalSellTime())));
                                            for (int i = 0; i < amount; i++) {
                                                MemorySellItem sellItem = new MemorySellItem(player, itemStack, price + random.nextInt(price / 2), time);
                                                SellItemEvent event = new SellItemEvent(user, sellItem);
                                                storage.validateAndAddItem(event);
                                                if (!event.isValid()) {
                                                    message.sendMsg(player, String.valueOf(event.getReason()));
                                                    break;
                                                }
                                            }
                                            message.sendMsg(player, "&aВы успешно выставили %s предметов на продажу за %s миллисекунд!", amount, timeCounter.getTime());
                                        }
                                )
                        )
                )
                .addSubCommand(new Command("sell")
                        .requires(new RequiresPermission("bauc.sell"))
                        .argument(new ArgumentIntegerAllowedMatch("price", List.of("[цена]")))
                        .argument(new ArgumentSetList("full", List.of("full"), List.of("full")))
                        .executor(((sender, args) -> {
                            if (!(sender instanceof Player player))
                                throw new CommandException("Вы должны быть игроком!");
                            int price = (int) args.getOrThrow("price", "&cВы должны указать цену!");
                            boolean full = !args.getOrDefault("full", "no").equals("full");

                            ItemStack itemStack = player.getInventory().getItemInMainHand();
                            if (itemStack.getType().isAir()) {
                                throw new CommandException("&cВы не можете торговать воздухом!");
                            }

                            MemoryUser user = storage.getMemoryUserOrCreate(player);

                            MemorySellItem sellItem = new MemorySellItem(player, itemStack, price, cfg.getDefaultSellTime() + user.getExternalSellTime(), full);
                            SellItemEvent event = new SellItemEvent(user, sellItem);
                            storage.validateAndAddItem(event);
                            if (event.isValid()) {
                                player.getInventory().setItemInMainHand(null);
                                message.sendMsg(player, "&aВы успешно выставили предмет на продажу!");
                            } else {
                                message.sendMsg(player, String.valueOf(event.getReason()));
                            }
                        }))
                )
                .addSubCommand(new Command("search")
                        .argument(new ArgumentStrings("tags"))
                        .executor((sender, args) -> {
                            if (!(sender instanceof Player player))
                                throw new CommandException("Вы должны быть игроком!");

                            String[] rawtags = ((String) args.getOrThrow("tags", "&cУкажите теги!")).split(" ");
                            List<String> tags = new ArrayList<>();
                            for (String rawtag : rawtags) {
                                tags.addAll(trieManager.getTrie().getAllWithPrefix(rawtag));
                            }
                            Category custom = cfg.getSorting().getAs("special.search", Category.class);
                            custom.setTags(new HashSet<>(tags));

                            MemoryUser user = storage.getMemoryUserOrCreate(player);

                            MainMenu menu = new MainMenu(user, player);
                            menu.setCustomCategory(custom);
                            menu.open();
                        })
                )
                .executor(((sender, args) -> {
                    if (!(sender instanceof Player player))
                        throw new CommandException("Вы должны быть игроком!");
                    MemoryUser user = storage.getMemoryUserOrCreate(player);
                    MainMenu menu = new MainMenu(user, player);
                    menu.open();
                }))
        ;
    }


    // @Override
    public boolean onCommand0(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
        try {
            command.process(sender, args);
            return true;
        } catch (CommandException e) {
            message.sendMsg(sender, e.getLocalizedMessage());
        }
        return true;
    }

    @Nullable
    //   @Override
    public List<String> onTabComplete0(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String alias, @NotNull String[] args) {
        if (args[0].equals("search")) {
            String last = args[args.length - 1];
            if (last.isEmpty()) return List.of("начните вводить название предмета");
            return trieManager.getTrie().getAllKeysWithPrefix(last);
        }
        return command.getTabCompleter(sender, args);
    }

}
