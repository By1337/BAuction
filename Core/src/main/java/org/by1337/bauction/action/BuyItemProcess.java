package org.by1337.bauction.action;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.by1337.api.util.CyclicList;
import org.by1337.bauction.Main;
import org.by1337.bauction.SellItem;
import org.by1337.bauction.User;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.impl.CallBack;
import org.by1337.bauction.menu.impl.ConfirmMenu;
import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.storage.event.BuyItemEvent;
import org.by1337.bauction.storage.event.TakeItemEvent;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.Sorting;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class BuyItemProcess {
    private final SellItem buyingItem;
    private final User buyer;
    private CyclicList<Category> categories;
    private CyclicList<Sorting> sortings;
    private int currentPage = -1;

    public BuyItemProcess(@NotNull SellItem buyingItem, @NotNull User buyer) {
        this.buyingItem = buyingItem;
        this.buyer = buyer;
    }

    public BuyItemProcess(@NotNull SellItem buyingItem, @NotNull User buyer, @Nullable CyclicList<Category> categories, @Nullable CyclicList<Sorting> sortings, int currentPage) {
        this.buyingItem = buyingItem;
        this.buyer = buyer;
        this.categories = categories;
        this.sortings = sortings;
        this.currentPage = currentPage;
    }

    public void process() {

        Player bukkitPlayer = Bukkit.getPlayer(buyer.getUuid());

        if (bukkitPlayer == null) {
            return;
        }
        SellItem item = Main.getStorage().update(buyingItem);
        User user = Main.getStorage().update(buyer);

        if (user == null) {
            Main.getMessage().sendMsg(bukkitPlayer, "&cЧто-то пошло не так!");
            Main.getMessage().error(new Throwable("lost user"));
            return;
        }

        if (item == null) {
            Main.getMessage().sendMsg(bukkitPlayer, "&cПредмет уже продан или снят с продажи!");
            createNewMenu(user, bukkitPlayer).open();
            return;
        }

        if (Main.getEcon().getBalance(bukkitPlayer) < item.getPrice()) {
            Main.getMessage().sendMsg(bukkitPlayer, "&cУ Вас не хватает баланса для покупки предмета!");
            createNewMenu(user, bukkitPlayer).open();
            return;
        }
        OfflinePlayer seller = Bukkit.getOfflinePlayer(item.getSellerUuid());

        CallBack<Optional<ConfirmMenu.Result>> callBack = result -> {
            if (result.isPresent()) {
                if (result.get() == ConfirmMenu.Result.ACCEPT) {
                    BuyItemEvent event = new BuyItemEvent(user, item);
                    Main.getStorage().validateAndRemoveItem(event);

                    if (event.isValid()) {
                        Main.getEcon().withdrawPlayer(bukkitPlayer, item.getPrice());
                        Main.getEcon().depositPlayer(seller, item.getPrice());
                        if (seller.isOnline()){
                            Main.getMessage().sendMsg(seller.getPlayer(), "&aИгрок %s купил у вас %s за %s!", bukkitPlayer.getName(), item.getMaterial(), item.getPrice());
                        }
                        Main.getMessage().sendMsg(bukkitPlayer, "&aВы успешно купили %s в количестве %s!", item.getMaterial(), item.getAmount());
                        Menu.giveItems(bukkitPlayer, item.getItemStack()).forEach(i -> bukkitPlayer.getLocation().getWorld().dropItem(bukkitPlayer.getLocation(), i));
                    } else {
                        Main.getMessage().sendMsg(bukkitPlayer, String.valueOf(event.getReason()));
                    }
                }
            }
            createNewMenu(user, bukkitPlayer).open();
        };

        ConfirmMenu confirmMenu = new ConfirmMenu(callBack, item.getItemStack());
        confirmMenu.setBukkitPlayer(bukkitPlayer);
        confirmMenu.addCustomPlaceHolders(user);
        confirmMenu.addCustomPlaceHolders(item);
        confirmMenu.open();
    }

    private MainMenu createNewMenu(User user, Player bukkitPlayer) {
        MainMenu menu = new MainMenu(user);
        menu.setBukkitPlayer(bukkitPlayer);
        if (categories != null)
            menu.setCategories(categories);
        if (sortings != null)
            menu.setSortings(sortings);
        if (currentPage != -1)
            menu.setCurrentPage(currentPage);
        return menu;
    }
}
