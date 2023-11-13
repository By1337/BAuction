package org.by1337.bauction.action;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.util.CyclicList;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.MemorySellItem;
import org.by1337.bauction.db.MemoryUser;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.impl.BuyCountMenu;
import org.by1337.bauction.menu.impl.CallBack;
import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.db.event.BuyItemCountEvent;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.Sorting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BuyItemCountProcess {

    private final MemorySellItem buyingItem;
    private final MemoryUser buyer;
    private CyclicList<Category> categories;
    private CyclicList<Sorting> sortings;
    private int currentPage = -1;

    public BuyItemCountProcess(@NotNull MemorySellItem buyingItem, @NotNull MemoryUser buyer) {
        this.buyingItem = buyingItem;
        this.buyer = buyer;
    }

    public BuyItemCountProcess(@NotNull MemorySellItem buyingItem, @NotNull MemoryUser buyer, @Nullable CyclicList<Category> categories, @Nullable CyclicList<Sorting> sortings, int currentPage) {
        this.buyingItem = buyingItem;
        this.buyer = buyer;
        this.categories = categories;
        this.sortings = sortings;
        this.currentPage = currentPage;
    }

    public void process() {
//
//        Player bukkitPlayer = Bukkit.getPlayer(buyer.getUuid());
//
//        if (bukkitPlayer == null) {
//            return;
//        }
//        MemorySellItem item = Main.getStorage().update(buyingItem);
//        User user = Main.getStorage().update(buyer);
//
//        if (user == null) {
//            Main.getMessage().sendMsg(bukkitPlayer, "&cЧто-то пошло не так!");
//            Main.getMessage().error(new Throwable("lost user"));
//            return;
//        }
//
//        if (item == null) {
//            Main.getMessage().sendMsg(bukkitPlayer, "&cПредмет уже продан или снят с продажи!");
//            createNewMenu(user, bukkitPlayer).open();
//            return;
//        }
//
//
//        OfflinePlayer seller = Bukkit.getOfflinePlayer(item.getSellerUuid());
//
//        CallBack<Optional<Integer>> callBack = result -> {
//            if (result.isPresent()) {
//                int count = result.get();
//
//                BuyItemCountEvent event = new BuyItemCountEvent(user, item, count);
//                Main.getStorage().validateAndRemoveItem(event);
//
//                if (event.isValid()) {
//                    double price = (item.getPriceForOne() * count);
//                    Main.getEcon().withdrawPlayer(bukkitPlayer, price);
//                    Main.getEcon().depositPlayer(seller, price);
//                    if (seller.isOnline()) {
//                        Main.getMessage().sendMsg(seller.getPlayer(), "&aИгрок %s купил у вас %s за %s!", bukkitPlayer.getName(), item.getMaterial(), NumberUtil.format(price));
//                    }
//                    Main.getMessage().sendMsg(bukkitPlayer, "&aВы успешно купили %s в количестве %s!", item.getMaterial(), item.getAmount());
//                    ItemStack itemStack = item.getItemStack();
//                    itemStack.setAmount(itemStack.getAmount() - count);
//                    Menu.giveItems(bukkitPlayer, itemStack).forEach(i -> bukkitPlayer.getLocation().getWorld().dropItem(bukkitPlayer.getLocation(), i));
//                } else {
//                    Main.getMessage().sendMsg(bukkitPlayer, String.valueOf(event.getReason()));
//                }
//            }
//            createNewMenu(user, bukkitPlayer).open();
//        };
//
//        BuyCountMenu buyCountMenu = new BuyCountMenu(user, item, callBack);
//        buyCountMenu.setBukkitPlayer(bukkitPlayer);
//        buyCountMenu.registerPlaceholderable(user);
//        buyCountMenu.registerPlaceholderable(item);
//        buyCountMenu.open();
    }

//    private MainMenu createNewMenu(User user, Player bukkitPlayer) {
//        MainMenu menu = new MainMenu(user);
//        menu.setBukkitPlayer(bukkitPlayer);
//        if (categories != null)
//            menu.setCategories(categories);
//        if (sortings != null)
//            menu.setSortings(sortings);
//        if (currentPage != -1)
//            menu.setCurrentPage(currentPage);
//        return menu;
//    }
}
