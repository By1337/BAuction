package org.by1337.bauction.menu;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.ItemHolder;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentInteger;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.menu.MenuItemBuilder;
import org.by1337.bmenu.menu.MenuLoader;
import org.by1337.bmenu.menu.MenuSetting;
import org.jetbrains.annotations.Nullable;

public class SelectCountMenu extends Menu implements ItemHolder {
    private int count = 1;
    private static final Command<SelectCountMenu> SELECT_COUNT_MENU_COMMAND;
    private final Cash cash;
    private static boolean seenIllegalCash;
    private SellItem sellItem;

    public SelectCountMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, MenuLoader menuLoader) {
        super(setting, player, previousMenu, menuLoader);
        if (setting.getCash() == null) {
            cash = new Cash();
            setting.setCash(cash);
        } else if (setting.getCash() instanceof Cash cash0) {
            this.cash = cash0;
        } else {
            if (!seenIllegalCash) {
                Main.getMessage().error("Illegal cash type '%s'! Excepted %s", setting.getCash().getClass(), Cash.class);
                seenIllegalCash = true;
            }
            cash = new Cash();
        }
        if (previousMenu != null) {
            if (previousMenu.getLastClickedItem() != null) {
                if (previousMenu.getLastClickedItem().getData() instanceof SellItem sellItem0) {
                    sellItem = sellItem0;
                    registerPlaceholders((Placeholder) sellItem);
                    registerPlaceholder("{count}", () -> count);
                    registerPlaceholder("{price_count}", () -> NumberUtil.format(sellItem.getPriceForOne() * count));
                }
            }
        }
    }

    @Override
    public void open() {
        if (sellItem == null) {
            Main.getMessage().error("Failed to open SelectCountMenu! %s, %s", previousMenu, previousMenu == null ? null : previousMenu.getLastClickedItem());
            return;
        }
        super.open();
    }

    @Override
    protected void generate() {
        customItems.clear();
        var item = cash.getItem().build(this, sellItem.getItemStack());
        item.getItemStack().setAmount(count);
        customItems.add(item);
    }

    public int getCount() {
        return count;
    }

    public SellItem getSellItem() {
        return sellItem;
    }

    @Override
    protected boolean runCommand(String[] cmd) throws CommandException {
        if (SELECT_COUNT_MENU_COMMAND.getSubcommands().containsKey(cmd[0])) {
            SELECT_COUNT_MENU_COMMAND.process(this, cmd);
            return true;
        }
        return false;
    }

    @Override
    public ItemStack getItemStack() {
        if (sellItem == null){
            return new ItemStack(Material.JIGSAW);
        }
        var item = sellItem.getItemStack();
        item.setAmount(count);
        return item;
    }

    private class Cash {
        private MenuItemBuilder item;

        public MenuItemBuilder getItem() {
            if (item == null) {
                item = setting.getContext().getAs("item", MenuItemBuilder.class);
            }
            return item;
        }
    }

    static {
        SELECT_COUNT_MENU_COMMAND = new Command<>("root");
        SELECT_COUNT_MENU_COMMAND.addSubCommand(
                new Command<SelectCountMenu>("[ADD]")
                        .argument(new ArgumentInteger<>("count"))
                        .executor((v, args) -> {
                            int x = (int) args.getOrThrow("count");
                            v.count += x;
                            if (v.count > v.sellItem.getAmount()) {
                                v.count = v.sellItem.getAmount();
                            }
                            v.refresh();
                        })
        );
        SELECT_COUNT_MENU_COMMAND.addSubCommand(
                new Command<SelectCountMenu>("[REMOVE]")
                        .argument(new ArgumentInteger<>("count"))
                        .executor((v, args) -> {
                            int x = (int) args.getOrThrow("count");
                            v.count -= x;
                            if (v.count < 1) {
                                v.count = 1;
                            }
                            v.refresh();
                        })
        );
    }
}
