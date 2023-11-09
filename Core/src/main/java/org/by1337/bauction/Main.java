package org.by1337.bauction;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.by1337.api.chat.util.Message;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.command.argument.ArgumentInteger;
import org.by1337.api.command.argument.ArgumentIntegerAllowedMatch;
import org.by1337.api.command.argument.ArgumentPlayer;
import org.by1337.api.command.argument.ArgumentSetList;
import org.by1337.api.command.requires.RequiresPermission;
import org.by1337.api.configuration.adapter.AdapterRegistry;
import org.by1337.api.configuration.adapter.impl.primitive.AdapterEnum;
import org.by1337.bauction.booost.Boost;
import org.by1337.bauction.config.Config;
import org.by1337.bauction.config.adapter.AdapterBoost;
import org.by1337.bauction.config.adapter.AdapterCategory;
import org.by1337.bauction.config.adapter.AdapterSortingType;
import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.storage.Storage;
import org.by1337.bauction.storage.event.SellItemEvent;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;
import org.by1337.bauction.util.TagUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public final class Main extends JavaPlugin {
    private static Message message;
    private static Plugin instance;
    private static Config cfg;
    private static Storage storage;
    private Command command;

    @Override
    public void onLoad() {
        instance = this;
        message = new Message(getLogger());

        AdapterRegistry.registerPrimitiveAdapter(Sorting.SortingType.class, new AdapterEnum<>(Sorting.SortingType.class));
        AdapterRegistry.registerAdapter(Sorting.class, new AdapterSortingType());
        AdapterRegistry.registerAdapter(Category.class, new AdapterCategory());
        AdapterRegistry.registerAdapter(Boost.class, new AdapterBoost());
    }

    @Override
    public void onEnable() {
        cfg = new Config(this);
        storage = new Storage(cfg.getCategoryMap(), cfg.getSortingMap());
        initCommand();
        getCommand("bauc").setTabCompleter(this);
        getCommand("bauc").setExecutor(this);
    }

    private void initCommand() {
        command = new Command("bauc")
                .requires(new RequiresPermission("bauc.use"))
                .addSubCommand(new Command("admin")
                        .requires(new RequiresPermission("bauc.admin"))
                        .addSubCommand(new Command("update")
                                .requires(new RequiresPermission("bauc.admin.update"))
                                .argument(new ArgumentPlayer("player"))
                                .executor((sender, args) -> {
                                    Player player = (Player) args.getOrThrow("player", "Вы должны указать игрока!");
                                    storage.updateUser(storage.getUserOrCreate(player).getUuid());
                                    message.sendMsg(sender, "&aИнформация о игроке успешно обновлена!");
                                })
                        )

                        .addSubCommand(new Command("push")
                                .requires(new RequiresPermission("bauc.admin.push"))
                                .argument(new ArgumentIntegerAllowedMatch("price", List.of("[цена]")))
                                .argument(new ArgumentInteger("amount", List.of("[количество]")))
                                .executor((sender, args) -> {
                                    int amount = (int) args.getOrDefault("amount", 1);
                                    int price = (int) args.getOrThrow("price", "&cВы должны указать цену!");

                                    if (!(sender instanceof Player player))
                                        throw new CommandException("Вы должны быть игроком!");

                                    ItemStack itemStack = player.getInventory().getItemInMainHand();
                                    if (itemStack.getType().isAir()) {
                                        throw new CommandException("&cВы не можете торговать воздухом!");
                                    }
                                    long x = System.currentTimeMillis();
                                    Random random = new Random();

                                    User user = storage.getUserOrCreate(player);
                                    for (int i = 0; i < amount; i++) {
                                        SellItem sellItem = new SellItem(player, itemStack, price + random.nextInt(price / 2), cfg.getDefaultSellTime() + user.getExternalSellTime());
                                        SellItemEvent event = new SellItemEvent(user, sellItem);
                                        storage.validateAndAddItem(event);
                                        if (!event.isValid()){
                                            message.sendMsg(player, String.valueOf(event.getReason()));
                                            break;
                                        }
                                    }

                                    message.sendMsg(player, "&aВы успешно выставили %s предметов на продажу за %s миллисекунд!", amount, (System.currentTimeMillis() - x));
                                })
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
                            boolean full = (boolean) args.getOrDefault("full", false);

                            ItemStack itemStack = player.getInventory().getItemInMainHand();
                            if (itemStack.getType().isAir()) {
                                throw new CommandException("&cВы не можете торговать воздухом!");
                            }



                            User user = storage.getUserOrCreate(player);

                            SellItem sellItem = new SellItem(player, itemStack, price, cfg.getDefaultSellTime() + user.getExternalSellTime(), full);
                            SellItemEvent event = new SellItemEvent(user, sellItem);
                            storage.validateAndAddItem(event);
                            if (event.isValid()){
                                player.getInventory().setItemInMainHand(null);
                                message.sendMsg(player, "&aВы успешно выставили предмет на продажу!");
                            }else {
                                message.sendMsg(player, String.valueOf(event.getReason()));
                            }
                        }))
                )
                .addSubCommand(new Command("parseTags")
                        .executor((sender, args) -> {
                            if (!(sender instanceof Player player))
                                throw new CommandException("Вы должны быть игроком!");
                            ItemStack itemStack = player.getInventory().getItemInMainHand();
                            if (itemStack.getType().isAir()) {
                                throw new CommandException("&cВы не можете торговать воздухом!");
                            }
                            message.sendMsg(sender, TagUtil.getTags(itemStack).toString());
                        })
                )
                .executor(((sender, args) -> {
                    if (!(sender instanceof Player player))
                        throw new CommandException("Вы должны быть игроком!");
                    User user = storage.getUserOrCreate(player);
                    MainMenu menu = new MainMenu(user);
                    menu.setBukkitPlayer(player);
                    menu.open();
                }))
        ;
    }


    @Override
    public void onDisable() {
        storage.end();
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

    public static Storage getStorage() {
        return storage;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String label, @NotNull String[] args) {
        try {
            command.process(sender, args);
            return true;
        } catch (CommandException e) {
            message.sendMsg(sender, e.getLocalizedMessage());
        }
        return true;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull org.bukkit.command.Command cmd, @NotNull String alias, @NotNull String[] args) {
        return command.getTabCompleter(sender, args);
    }
}
