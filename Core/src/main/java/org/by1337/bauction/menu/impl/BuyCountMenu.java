package org.by1337.bauction.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentInteger;
import org.by1337.blib.command.argument.ArgumentStrings;
import org.by1337.blib.util.Pair;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.command.DefaultMenuCommand;
import org.by1337.bauction.util.NumberUtil;

import java.util.Optional;

public class BuyCountMenu extends Menu {

    private final Command<Pair<Menu, Player>> command;
    private final User user;

    private int count = 1;
    private final SellItem item;
    private final CustomItemStack customItemStack;
    private final CallBack<Optional<Integer>> callBack;

    public BuyCountMenu(User user, SellItem item, CallBack<Optional<Integer>> callBack, Player player) {
        this(user, item, callBack, player, null);
    }

    public BuyCountMenu(User user, SellItem item, CallBack<Optional<Integer>> callBack, Player player, Menu backMenu) {
        super(Main.getCfg().getMenuManger().getMenuBuyCount(), player, backMenu, user);
        this.user = user;
        this.item = item;
        this.callBack = callBack;
        customItemStack = Main.getCfg().getMenuBuyCount().getAs("item", CustomItemStack.class);
        customItemStack.setItemStack(item.getItemStack());
        customItemStack.registerPlaceholder(item);
        customItemStack.registerPlaceholder(this);
        customItemStacks.add(customItemStack);
        registerPlaceholderable(user);
        registerPlaceholderable(item);

        command = new Command<Pair<Menu, Player>>("")
                .addSubCommand(new Command<Pair<Menu, Player>>("[CLOSE]")
                        .executor(((sender, args) -> viewer.closeInventory()))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[CONSOLE]")
                        .argument(new ArgumentStrings<>("cmd"))
                        .executor(((sender, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[PLAYER]")
                        .argument(new ArgumentStrings<>("cmd"))
                        .executor(((sender, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            viewer.performCommand(cmd);
                        }))
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[ADD]")
                        .argument(new ArgumentInteger<>("count"))
                        .executor((sender, args) -> {
                            int x = (int) args.getOrThrow("count");
                            count += x;
                            if (count > item.getAmount()) {
                                count = item.getAmount();
                            }
                            customItemStack.setAmount(count);
                            generate0();
                        })
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[REMOVE]")
                        .argument(new ArgumentInteger<>("count"))
                        .executor((sender, args) -> {
                            int x = (int) args.getOrThrow("count");
                            count -= x;
                            if (count < 1) {
                                count = 1;
                            }
                            customItemStack.setAmount(count);
                            generate0();
                        })
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[BUY]")
                        .executor((sender, args) -> {
                            if (Main.getEcon().getBalance(getPlayer()) < (item.getPriceForOne() * count)) {
                                Main.getMessage().sendMsg(getPlayer(), Lang.getMessage("insufficient_balance"));
                                generate0();
                                return;
                            }
                            syncUtil(() -> callBack.result(Optional.of(count)));
                        })
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[CANCEL]")
                        .executor((sender, args) -> {
                            syncUtil(() -> callBack.result(Optional.empty()));
                            generate0();
                        })
                );
        DefaultMenuCommand.command.getSubcommands().forEach((s, c) -> command.addSubCommand(c));
    }

    @Override
    public void onClose(InventoryCloseEvent e) {
        super.onClose(e);
        syncUtil(() -> callBack.result(Optional.empty()));
    }

    @Override
    protected void generate() {

    }

    @Override
    public void runCommand(Placeholderable holder, String... commands) {
        try {
            for (String cmd : commands) {
                command.process(new Pair<>(this, viewer), holder.replace(cmd).split(" "));
            }
        } catch (CommandException e) {
            Main.getMessage().error(e);
        }
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(Main.getMessage().messageBuilder(s, viewer));
        while (true) {
            if (sb.indexOf("{count}") != -1) {
                sb.replace(sb.indexOf("{count}"), sb.indexOf("{count}") + "{count}".length(), String.valueOf(count));
                continue;
            }
            if (sb.indexOf("{price_count}") != -1) {
                sb.replace(sb.indexOf("{price_count}"), sb.indexOf("{price_count}") + "{price_count}".length(), item == null ? "" : NumberUtil.format(item.getPriceForOne() * count));
                continue;
            }

            break;
        }
        String str = sb.toString();
        for (Placeholderable val : customPlaceHolders) {
            str = val.replace(str);
        }
        return str;
    }

    public void reopen() {
        if (getPlayer() == null || !getPlayer().isOnline()) {
            throw new IllegalStateException("player is offline!");
        }
        syncUtil(() -> {
            if (!viewer.getOpenInventory().getTopInventory().equals(inventory)) {
                viewer.openInventory(getInventory());
                reRegister();
            }
            sendFakeTitle(replace(title));
            generate0();
        });
    }
}
