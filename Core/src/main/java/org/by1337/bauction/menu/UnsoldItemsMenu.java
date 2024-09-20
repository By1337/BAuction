package org.by1337.bauction.menu;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.PluginUnsoldItem;
import org.by1337.bauction.db.kernel.PluginUser;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.bmenu.menu.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UnsoldItemsMenu extends Menu {
    private static final Command<UnsoldItemsMenu> UNSOLD_ITEMS_MENU_COMMAND;
    protected int currentPage = 0;
    protected int maxPage = 0;

    protected PluginUser user;
    private final Cache cache;
    private static boolean seenIllegalCash;
    private final List<PluginUnsoldItem> unsoldItems = new ArrayList<>();

    public UnsoldItemsMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, MenuLoader menuLoader) {
        super(setting, player, previousMenu, menuLoader);
        if (setting.getCache() == null) {
            cache = new Cache();
            setting.setCache(cache);
        } else if (setting.getCache() instanceof Cache cache0) {
            this.cache = cache0;
        } else {
            if (!seenIllegalCash) {
                Main.getMessage().error("Illegal cache type '%s'! Excepted %s", setting.getCache().getClass(), Cache.class);
                seenIllegalCash = true;
            }
            cache = new Cache();
        }
        init();
    }

    private void init() {
        user = (PluginUser) Main.getStorage().getUserOrCreate(viewer);
        registerPlaceholder("{max_page}", () -> maxPage == 0 ? 1 : maxPage);
        registerPlaceholder("{current_page}", () -> currentPage + 1);
        registerPlaceholders(user);
    }

    @Override
    protected void generate() {
        setSellItems();
        Iterator<Integer> slotsIterator = cache.getSlots().listIterator();
        customItems.clear();
        for (int x = currentPage * cache.getSlots().size(); x < unsoldItems.size(); x++) {
            PluginUnsoldItem item = unsoldItems.get(x);
            if (slotsIterator.hasNext()) {
                int slot = slotsIterator.next();

                MenuItemBuilder menuItemBuilder = cache.getItem();

                MenuItem menuItem = menuItemBuilder.build(this, item.getItemStack(), item);

                menuItem.getItemStack().setAmount(item.getItemStack().getAmount());
                menuItem.setData(item);
                menuItem.setSlots(new int[]{slot});
                customItems.add(menuItem);
            } else {
                break;
            }
        }
    }

    protected void setSellItems() {
        unsoldItems.clear();
        Main.getStorage().forEachUnsoldItemsByUser(unsoldItems::add, user.getUuid());
        maxPage = (int) Math.ceil((double) unsoldItems.size() / cache.getSlots().size());
        if (currentPage > maxPage) {
            currentPage = maxPage - 1;
            if (currentPage < 0) currentPage = 0;
        }
        if (currentPage * cache.getSlots().size() >= unsoldItems.size()) {
            maxPage = 0;
        }
    }

    @Override
    protected boolean runCommand(String[] cmd) throws CommandException {
        if (UNSOLD_ITEMS_MENU_COMMAND.getSubcommands().containsKey(cmd[0])) {
            UNSOLD_ITEMS_MENU_COMMAND.process(this, cmd);
            return true;
        }
        return false;
    }

    static {
        UNSOLD_ITEMS_MENU_COMMAND = new Command<>("root");
        UNSOLD_ITEMS_MENU_COMMAND.addSubCommand(
                new Command<UnsoldItemsMenu>("[NEXT_PAGE]")
                        .executor((menu, args) -> {
                            if (menu.currentPage < menu.maxPage - 1) {
                                menu.currentPage++;
                                menu.refresh();
                            }
                        })
        );
        UNSOLD_ITEMS_MENU_COMMAND.addSubCommand(
                new Command<UnsoldItemsMenu>("[PREVIOUS_PAGE]")
                        .executor((menu, args) -> {
                            if (menu.currentPage > 0) {
                                menu.currentPage--;
                                menu.refresh();
                            }
                        })
        );
    }

    private class Cache {
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
