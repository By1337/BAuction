package org.by1337.bauction.action;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.MemorySellItem;

import org.by1337.bauction.db.MemoryUser;
import org.by1337.bauction.db.event.BuyItemEvent;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.impl.CallBack;
import org.by1337.bauction.menu.impl.ConfirmMenu;
import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.util.NumberUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BuyItemProcess implements Placeholderable {
    private final MemorySellItem buyingItem;
    private final MemoryUser buyer;
    private final Menu menu;
    private final Player player;

    public BuyItemProcess(@NotNull MemorySellItem buyingItem, @NotNull MemoryUser buyer, Menu menu, Player player) {
        this.buyingItem = buyingItem;
        this.buyer = buyer;
        this.menu = menu;
        this.player = player;
    }


    public void process() {
       try {
           if (Main.getEcon().getBalance(player) < buyingItem.getPrice()) {
               Main.getMessage().sendMsg(player, "&cУ Вас не хватает баланса для покупки предмета!");
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
                                       replace("&aИгрок {buyer_name} купил у вас {item_name}&r за {price}!"));
                           }
                           Main.getMessage().sendMsg(player, replace("&aВы успешно купили {item_name}&r в количестве {amount}!"));
                           Menu.giveItems(player, buyingItem.getItemStack()).forEach(i -> player.getLocation().getWorld().dropItem(player.getLocation(), i));
                       } else {
                           Main.getMessage().sendMsg(player, String.valueOf(event.getReason()));
                       }
                   }
               }
               menu.reopen();
           };

           ConfirmMenu confirmMenu = new ConfirmMenu(callBack, buyingItem.getItemStack(), player);
           confirmMenu.registerPlaceholderable(buyer);
           confirmMenu.registerPlaceholderable(buyingItem);

           confirmMenu.open();

       }catch (Exception e){
           Main.getMessage().sendMsg(player, "&cЧто-то пошло не так!");
           Main.getMessage().error(e);
       }
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (true) {
            if (sb.indexOf("{amount}") != -1) {
                sb.replace(sb.indexOf("{amount}"), sb.indexOf("{amount}") + "{amount}".length(), String.valueOf(buyingItem.getAmount()));
                continue;
            }
            if (sb.indexOf("{buyer_name}") != -1) {
                sb.replace(sb.indexOf("{buyer_name}"), sb.indexOf("{buyer_name}") + "{buyer_name}".length(), player.getName());
                continue;
            }
            if (sb.indexOf("{price}") != -1) {
                sb.replace(sb.indexOf("{price}"), sb.indexOf("{price}") + "{price}".length(), NumberUtil.format(buyingItem.getPrice()));
                continue;
            }
            if (sb.indexOf("{item_name}") != -1) {
                sb.replace(sb.indexOf("{item_name}"), sb.indexOf("{item_name}") + "{item_name}".length(),
                        buyingItem.getItemStack().getItemMeta() != null && buyingItem.getItemStack().getItemMeta().hasDisplayName() ?
                                buyingItem.getItemStack().getItemMeta().getDisplayName() :
                                buyingItem.getMaterial().name()
                        );
                continue;
            }
            break;
        }
        return sb.toString();
    }
}
