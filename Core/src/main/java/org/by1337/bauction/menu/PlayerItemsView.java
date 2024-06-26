package org.by1337.bauction.menu;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.util.auction.TagUtil;
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
        registerPlaceholder("{user_name}", () -> String.valueOf(this.name));
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

        maxPage = (int) Math.ceil((double) sellItems.size() / cache.getSlots().size());

        if (currentPage > maxPage) {
            currentPage = maxPage - 1;
            if (currentPage < 0) currentPage = 0;
        }

        if (currentPage * cache.getSlots().size() >= sellItems.size()) {
            maxPage = 0;
        }
    }

}
