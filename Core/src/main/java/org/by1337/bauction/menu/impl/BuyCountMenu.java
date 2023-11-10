package org.by1337.bauction.menu.impl;

import org.bukkit.configuration.MemorySection;
import org.by1337.api.chat.Placeholderable;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.command.argument.ArgumentInteger;
import org.by1337.bauction.Main;
import org.by1337.bauction.SellItem;
import org.by1337.bauction.User;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.MenuFactory;
import org.by1337.bauction.util.NumberUtil;

import java.util.Optional;

public class BuyCountMenu extends Menu {

    private final Command command;
    private final User user;

    private int count = 1;
    private final SellItem item;
    private final CustomItemStack customItemStack;
    private final CallBack<Optional<Integer>> callBack;

    public BuyCountMenu(User user, SellItem item, CallBack<Optional<Integer>> callBack) {
        super(MenuFactory.create(Main.getCfg().getMenuBuyCount()));
        this.user = user;
        this.item = item;
        this.callBack = callBack;
        customItemStack = MenuFactory.menuItemBuilder(((MemorySection) super.menuFile.get("item")).getValues(false));
        customItemStack.setItemStack(item.getItemStack());
        customItemStack.registerPlaceholder(item);
        customItemStack.registerPlaceholder(this);
        customItemStacks.add(customItemStack);

        command = new Command("")
                .addSubCommand(new Command("[ADD]")
                        .argument(new ArgumentInteger("count"))
                        .executor((sender, args) -> {
                            int x = (int) args.getOrThrow("count");
                            count += x;
                            if (count > item.getAmount()) {
                                count = item.getAmount();
                            }
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
                            generate0();
                        })
                )
                .addSubCommand(new Command("[BUY]")
                        .executor((sender, args) -> {
                            callBack.result(Optional.of(count));
                            generate0();
                        })
                )
                .addSubCommand(new Command("[CANCEL]")
                        .executor((sender, args) -> {
                            callBack.result(Optional.empty());
                            generate0();
                        })
                );
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
        StringBuilder sb = new StringBuilder(s);
        while (true) {
            if (sb.indexOf("{count}") != -1) {
                sb.replace(sb.indexOf("{count}"), sb.indexOf("{count}") + "{count}".length(), String.valueOf(count));
                continue;
            }
            if (sb.indexOf("{price}") != -1) {
                sb.replace(sb.indexOf("{price}"), sb.indexOf("{price}") + "{price}".length(), NumberUtil.format(item.getPriceForOne() * count));
                continue;
            }

            break;
        }
        return sb.toString();
    }
}
