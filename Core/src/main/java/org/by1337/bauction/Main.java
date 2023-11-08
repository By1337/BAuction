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
import org.by1337.api.command.argument.ArgumentSetList;
import org.by1337.api.command.requires.RequiresPermission;
import org.by1337.api.configuration.adapter.AdapterRegistry;
import org.by1337.api.configuration.adapter.impl.primitive.AdapterEnum;
import org.by1337.bauction.config.Config;
import org.by1337.bauction.config.adapter.AdapterCategory;
import org.by1337.bauction.config.adapter.AdapterSortingType;
import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.storage.Storage;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;
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
                .addSubCommand(new Command("sell")
                        .requires(new RequiresPermission("bauc.sell"))
                        .argument(new ArgumentIntegerAllowedMatch("price", List.of("[цена]")))
                        .argument(new ArgumentInteger("amount", List.of("?[количество]")))
                        .argument(new ArgumentSetList("full",List.of("?[продавать только полностью]"), List.of("-full")))
                        .executor(((sender, args) -> {
                            if (!(sender instanceof Player player))
                                throw new CommandException("Вы должны быть игроком!");
                            int price = (int) args.getOrThrow("price", "&cВы должны указать цену!");
                            int amount = (int) args.getOrDefault("amount", 1000);

                            ItemStack itemStack = player.getInventory().getItemInMainHand();
                            if (itemStack.getType().isAir()){
                                throw new CommandException("&cВы не можете торговать воздухом!");
                            }
                            long x = System.currentTimeMillis();
                            player.getInventory().setItemInMainHand(null);
                            Random random = new Random();
                            for (int i = 0; i < amount; i++) {
                                SellItem sellItem = new SellItem(player, itemStack, price + random.nextInt(5000), 99999999L);
                                storage.addItem(sellItem);
                                //     storage.sort();
                            }
                            message.sendMsg(player, "&aВы успешно выставили предмет на продажу! за " + (System.currentTimeMillis() - x));

                        }))
                )
                .executor(((sender, args) -> {
                    if (!(sender instanceof Player player))
                        throw new CommandException("Вы должны быть игроком!");
                    MainMenu menu = new MainMenu();
                    menu.setBukkitPlayer(player);
                    menu.open();
                }))
        ;
    }


    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
