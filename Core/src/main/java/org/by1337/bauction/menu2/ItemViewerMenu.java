package org.by1337.bauction.menu2;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.ItemHolder;
import org.by1337.bauction.lang.Lang;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.command.CommandException;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.menu.MenuItemBuilder;
import org.by1337.bmenu.menu.MenuLoader;
import org.by1337.bmenu.menu.MenuSetting;
import org.jetbrains.annotations.Nullable;

public class ItemViewerMenu extends Menu {
    private final Cash cash;
    private ItemStack itemStack;
    private Object data;
    private static boolean seenIllegalCash;

    public ItemViewerMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, MenuLoader menuLoader) {
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
            var item = previousMenu.getLastClickedItem();
            if (item != null && item.getData() instanceof ItemHolder itemHolder) {
                data = item.getData();
                itemStack = itemHolder.getItemStack();
                if (data instanceof Placeholder placeholder) {
                    registerPlaceholders(placeholder);
                }
            } else {
                Menu m = this;
                while (!(m instanceof ItemHolder) && m != null) {
                    m = m.getPreviousMenu();
                }
                if (m instanceof ItemHolder itemHolder) {
                    itemStack = itemHolder.getItemStack();
                }
                if (m != null && m != this) {
                    registerPlaceholders(m);
                }
            }
        }
    }

    @Override
    protected void generate() {
        if (itemStack != null) {
            customItems.clear();
            var item = cash.getItem().build(this, itemStack);
            item.setData(data);
            customItems.add(item);
        }
    }

    @Override
    protected boolean runCommand(String[] cmd) throws CommandException {
        return false;
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
}
