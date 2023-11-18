package org.by1337.bauction.action;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.SellItem;

import org.by1337.bauction.db.kernel.User;
import org.by1337.bauction.db.event.BuyItemEvent;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.impl.CallBack;
import org.by1337.bauction.menu.impl.ConfirmMenu;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.PlayerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BuyItemProcess implements Placeholderable {
    private final SellItem buyingItem;
    private final User buyer;
    private final Menu menu;
    private final Player player;
    private final boolean fast;

    public BuyItemProcess(@NotNull SellItem buyingItem, @NotNull User buyer, Menu menu, Player player) {
        this(buyingItem, buyer, menu,player, false);
    }
    public BuyItemProcess(@NotNull SellItem buyingItem, @NotNull User buyer, Menu menu, Player player, boolean fast) {
        this.buyingItem = buyingItem;
        this.buyer = buyer;
        this.menu = menu;
        this.player = player;
        this.fast = fast;
    }


    public void process() {
       try {
           if (Main.getEcon().getBalance(player) < buyingItem.getPrice()) {
               Main.getMessage().sendMsg(player, Lang.getMessages("insufficient_balance"));
               menu.reopen();
               return;
           }

           CallBack<Optional<ConfirmMenu.Result>> callBack = result -> {
               if (result.isPresent()) {
                   if (result.get() == ConfirmMenu.Result.ACCEPT) {
                       BuyItemEvent event = new BuyItemEvent(buyer, buyingItem);
                       Main.getStorage().validateAndRemoveItem(event);

                       OfflinePlayer seller = Bukkit.getOfflinePlayer(buyingItem.getSellerUuid());
                       if (event.isValid()) {
                           Main.getEcon().withdrawPlayer(player, buyingItem.getPrice());
                           Main.getEcon().depositPlayer(seller, buyingItem.getPrice());
                           if (seller.isOnline()){
                               Main.getMessage().sendMsg(seller.getPlayer(),
                                       replace(Lang.getMessages("item_sold_to_buyer")));
                           }
                           Main.getMessage().sendMsg(player, replace(Lang.getMessages("successful_purchase")));
                           PlayerUtil.giveItems(player, buyingItem.getItemStack());
                       } else {
                           Main.getMessage().sendMsg(player, String.valueOf(event.getReason()));
                       }
                   }
               }
               menu.reopen();
           };

           if (fast){
               callBack.result(Optional.of(ConfirmMenu.Result.ACCEPT));
           }else {
               ConfirmMenu confirmMenu = new ConfirmMenu(callBack, buyingItem.getItemStack(), player);
               confirmMenu.registerPlaceholderable(buyer);
               confirmMenu.registerPlaceholderable(buyingItem);

               confirmMenu.open();
           }



       }catch (Exception e){
           Main.getMessage().sendMsg(player, Lang.getMessages("something_went_wrong"));
           Main.getMessage().error(e);
       }
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(buyingItem.replace(s));
        while (true) {
            if (sb.indexOf("{buyer_name}") != -1) {
                sb.replace(sb.indexOf("{buyer_name}"), sb.indexOf("{buyer_name}") + "{buyer_name}".length(), player.getName());
                continue;
            }
            break;
        }
        return sb.toString();
    }
}
