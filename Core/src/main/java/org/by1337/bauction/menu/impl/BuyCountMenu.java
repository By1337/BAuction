package org.by1337.bauction.menu.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.by1337.api.chat.Placeholderable;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.command.argument.ArgumentInteger;
import org.by1337.api.command.argument.ArgumentStrings;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.db.kernel.CUser;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.util.NumberUtil;

import java.util.Optional;

public class BuyCountMenu extends Menu {

    private final Command command;
    private final CUser user;

    private int count = 1;
    private final CSellItem item;
    private final CustomItemStack customItemStack;
    private final CallBack<Optional<Integer>> callBack;

    public BuyCountMenu(CUser user, CSellItem item, CallBack<Optional<Integer>> callBack, Player player) {
        super(Main.getCfg().getMenuManger().getMenuBuyCount(), player);
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

        command = new Command("")
                .addSubCommand(new Command("[CONSOLE]")
                        .argument(new ArgumentStrings("cmd"))
                        .executor(((sender, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
                        }))
                )
                .addSubCommand(new Command("[PLAYER]")
                        .argument(new ArgumentStrings("cmd"))
                        .executor(((sender, args) -> {
                            String cmd = (String) args.getOrThrow("cmd");
                            viewer.performCommand(cmd);
                        }))
                )
                .addSubCommand(new Command("[ADD]")
                        .argument(new ArgumentInteger("count"))
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
                .addSubCommand(new Command("[REMOVE]")
                        .argument(new ArgumentInteger("count"))
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
                .addSubCommand(new Command("[BUY]")
                        .executor((sender, args) -> {
                            if (Main.getEcon().getBalance(getPlayer()) < (item.getPriceForOne() * count)) {
                                Main.getMessage().sendMsg(getPlayer(), Lang.getMessages("insufficient_balance"));
                                generate0();
                                return;
                            }
                            syncUtil(() -> callBack.result(Optional.of(count)));
                        })
                )
                .addSubCommand(new Command("[CANCEL]")
                        .executor((sender, args) -> {
                            syncUtil(() -> callBack.result(Optional.empty()));
                            generate0();
                        })
                );
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
                command.process(null, holder.replace(cmd).split(" "));
            }
        } catch (CommandException e) {
            Main.getMessage().error(e);
        }
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(Main.getMessage().messageBuilder(s, getPlayer()));
        while (true) {
            if (sb.indexOf("{count}") != -1) {
                sb.replace(sb.indexOf("{count}"), sb.indexOf("{count}") + "{count}".length(), String.valueOf(count));
                continue;
            }
            if (sb.indexOf("{price_count}") != -1) {
                sb.replace(sb.indexOf("{price_count}"), sb.indexOf("{price_count}") + "{price_count}".length(), NumberUtil.format(item.getPriceForOne() * count));
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
            throw new IllegalArgumentException();
        }
        syncUtil(() -> {
            reRegister();
            if (!viewer.getOpenInventory().getTopInventory().equals(inventory))
                viewer.openInventory(getInventory());
            sendFakeTitle(replace(title));
            generate0();
        });
    }
}
