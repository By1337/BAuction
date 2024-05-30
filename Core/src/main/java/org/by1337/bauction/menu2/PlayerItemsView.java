package org.by1337.bauction.menu2;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.util.TagUtil;
import org.by1337.blib.command.CommandException;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.menu.MenuLoader;
import org.by1337.bmenu.menu.MenuSetting;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerItemsView extends HomeMenu {
    private UUID uuid;
    private boolean hasUser;
    private String name;
    public PlayerItemsView(MenuSetting setting, Player player, @Nullable Menu previousMenu, MenuLoader menuLoader) {
        super(setting, player, previousMenu, menuLoader);
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
        init();
    }

    public void setName(String name) {
        this.name = name;
    }

    private void init(){
        hasUser = Main.getStorage().hasUser(uuid);
    }
    @Override
    protected void setSellItems() {
        sellItems = new ArrayList<>();
        if (!hasUser) {
            return;
        }
        lastCategory = categories.getCurrent();
        lastSorting = sortings.getCurrent();
        lastPage = currentPage;

        Main.getStorage().forEachSellItemsByUser(item -> {
            if (TagUtil.matchesCategory(lastCategory, item)) {
                sellItems.add(item);
            }
        }, uuid);
        sellItems.sort(lastSorting.getComparator());

        maxPage = (int) Math.ceil((double) sellItems.size() / cash.getSlots().size());

        if (currentPage > maxPage) {
            currentPage = maxPage - 1;
            if (currentPage < 0) currentPage = 0;
        }

        if (currentPage * cash.getSlots().size() >= sellItems.size()) {
            maxPage = 0;
        }
    }

}
