package org.by1337.bauction.menu;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.db.kernel.CUser;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.bmenu.menu.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ItemsForSaleMenu extends Menu {
    private static final Command<ItemsForSaleMenu> ITEMS_FOR_SALE_MENU_COMMAND;
    protected int currentPage = 0;
    protected int maxPage = 0;
    protected CUser user;
    private final Cash cash;
    private static boolean seenIllegalCash;
    private final List<SellItem> sellItems = new ArrayList<>();

    public ItemsForSaleMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, MenuLoader menuLoader) {
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
        init();
    }

    private void init() {
        user = (CUser) Main.getStorage().getUserOrCreate(viewer);
        registerPlaceholder("{max_page}", () -> maxPage == 0 ? 1 : maxPage);
        registerPlaceholder("{current_page}", () -> currentPage + 1);
        registerPlaceholders(user);
    }

    @Override
    protected void generate() {
        setSellItems();
        Iterator<Integer> slotsIterator = cash.getSlots().listIterator();
        customItems.clear();
        for (int x = currentPage * cash.getSlots().size(); x < sellItems.size(); x++) {
            SellItem item = sellItems.get(x);
            if (slotsIterator.hasNext()) {
                int slot = slotsIterator.next();

                MenuItemBuilder menuItemBuilder = cash.getItem();

                MenuItem menuItem = menuItemBuilder.build(this, item.getItemStack(), item);

                menuItem.getItemStack().setAmount(item.getAmount());
                menuItem.setData(item);
                menuItem.setSlots(new int[]{slot});
                customItems.add(menuItem);
            } else {
                break;
            }
        }
    }

    protected void setSellItems() {
        sellItems.clear();
        Main.getStorage().forEachSellItemsByUser(sellItems::add, user.getUuid());
        maxPage = (int) Math.ceil((double) sellItems.size() / cash.getSlots().size());
        if (currentPage > maxPage) {
            currentPage = maxPage - 1;
            if (currentPage < 0) currentPage = 0;
        }
        if (currentPage * cash.getSlots().size() >= sellItems.size()) {
            maxPage = 0;
        }
    }

    @Override
    protected boolean runCommand(String[] cmd) throws CommandException {
        if (ITEMS_FOR_SALE_MENU_COMMAND.getSubcommands().containsKey(cmd[0])) {
            ITEMS_FOR_SALE_MENU_COMMAND.process(this, cmd);
            return true;
        }
        return false;
    }

    static {
        ITEMS_FOR_SALE_MENU_COMMAND = new Command<>("root");
        ITEMS_FOR_SALE_MENU_COMMAND.addSubCommand(
                new Command<ItemsForSaleMenu>("[NEXT_PAGE]")
                        .executor((menu, args) -> {
                            if (menu.currentPage < menu.maxPage - 1) {
                                menu.currentPage++;
                                menu.refresh();
                            }
                        })
        );
        ITEMS_FOR_SALE_MENU_COMMAND.addSubCommand(
                new Command<ItemsForSaleMenu>("[PREVIOUS_PAGE]")
                        .executor((menu, args) -> {
                            if (menu.currentPage > 0) {
                                menu.currentPage--;
                                menu.refresh();
                            }
                        })
        );
    }

    private class Cash {
        private List<Integer> slots;
        private MenuItemBuilder item;

        public List<Integer> getSlots() {
            if (slots == null) {
                slots = MenuFactory.getSlots(setting.getContext().getList("items-slots", String.class));
            }
            return slots;
        }

        public MenuItemBuilder getItem() {
            if (item == null) {
                item = setting.getContext().getAs("item", MenuItemBuilder.class);
            }
            return item;
        }

    }
}
