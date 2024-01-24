package org.by1337.bauction.menu.impl;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.util.TagUtil;

import java.util.ArrayList;
import java.util.UUID;

public class PlayerItemsView extends MainMenu {

    private final UUID uuid;
    private final boolean hasUser;
    private final String name;

    public PlayerItemsView(User user, Player player, UUID uuid, String name) {
        super(Main.getCfg().getMenuManger().getMenuPlayerItemsView(),
                player, null, user, Main.getCfg().getMenuManger().getPlayerItemsViewSlots());
        this.uuid = uuid;
        hasUser = Main.getStorage().hasUser(uuid);
        this.name = name;
    }


    @Override
    protected void setSellItems() {
        if (!hasUser) {
            setSellItems(new ArrayList<>());
            return;
        }
        lastCategory = categories.getCurrent();
        lastSorting = sortings.getCurrent();
        lastPage = currentPage;

        sellItems = new ArrayList<>();
        Main.getStorage().forEachSellItemsByUser(item -> {
            if (TagUtil.matchesCategory(lastCategory, item)) {
                sellItems.add(item);
            }
        }, uuid);
//        for (SellItem item : Main.getStorage().getSellItemsByUser(uuid)) {
//            if (TagUtil.matchesCategory(lastCategory, item)) {
//                sellItems.add(item);
//            }
//        }
        sellItems.sort(lastSorting.getComparator());

        maxPage = (int) Math.ceil((double) sellItems.size() / slots.size());

        if (currentPage > maxPage) {
            currentPage = maxPage - 1;
            if (currentPage < 0) currentPage = 0;
        }

        if (currentPage * slots.size() >= sellItems.size()) {
            maxPage = 0;
        }
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (true) {
            if (sb.indexOf("{user_name}") != -1) {
                sb.replace(sb.indexOf("{user_name}"), sb.indexOf("{user_name}") + "{user_name}".length(), String.valueOf(name));
                continue;
            }
            break;
        }
        String str = sb.toString();
        return super.replace(str);
    }
}
