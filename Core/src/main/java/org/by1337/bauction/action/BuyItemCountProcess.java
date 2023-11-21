package org.by1337.bauction.action;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.db.kernel.СUser;
import org.by1337.bauction.db.event.BuyItemCountEvent;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.impl.BuyCountMenu;
import org.by1337.bauction.menu.impl.CallBack;
import org.by1337.bauction.menu.impl.ConfirmMenu;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.PlayerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BuyItemCountProcess {

    private final CSellItem buyingItem;
    private final СUser buyer;
    private final Player player;
    private final Menu menu;
    private final boolean fast;

    public BuyItemCountProcess(@NotNull CSellItem buyingItem, @NotNull СUser buyer, Player player, Menu menu, boolean fast) {
        this.buyingItem = buyingItem;
        this.buyer = buyer;
        this.player = player;
        this.menu = menu;
        this.fast = fast;
    }

    private CallBack<Optional<Integer>> callBack;
    private BuyCountMenu buyCountMenu;
    private int count = 0;

    public void process() {
        try {

            CallBack<Optional<ConfirmMenu.Result>> callBack1 = result1 -> {
                if (result1.isPresent()) {
                    if (result1.get() == ConfirmMenu.Result.ACCEPT) {
                        BuyItemCountEvent event = new BuyItemCountEvent(buyer, buyingItem, count);
                        Main.getStorage().validateAndRemoveItem(event);

                        OfflinePlayer seller = Bukkit.getOfflinePlayer(buyingItem.getSellerUuid());

                        if (event.isValid()) {
                            double price = (buyingItem.getPriceForOne() * count);
                            Main.getEcon().withdrawPlayer(player, price);
                            Main.getEcon().depositPlayer(seller, price);
                            if (seller.isOnline()) {
                                Main.getMessage().sendMsg(seller.getPlayer(),
                                        replace(Lang.getMessages("item_sold_to_buyer")));
                            }
                            Main.getMessage().sendMsg(player, replace(Lang.getMessages("successful_purchase")));
                            ItemStack itemStack = buyingItem.getItemStack();
                            itemStack.setAmount(itemStack.getAmount() - count);
                            PlayerUtil.giveItems(player, itemStack);
                        } else {
                            Main.getMessage().sendMsg(player, String.valueOf(event.getReason()));
                        }
                        menu.reopen();
                        return;
                    }
                    buyCountMenu.reopen();
                }
            };

            callBack = result -> {
                if (result.isPresent()) {
                    count = result.get();
                    if (fast){
                        callBack1.result(Optional.of(ConfirmMenu.Result.ACCEPT));
                    }else {
                        ItemStack itemStack = buyingItem.getItemStack();
                        itemStack.setAmount(count);
                        new ConfirmMenu(callBack1, itemStack, player).open();
                    }
                }
            };

            buyCountMenu = new BuyCountMenu(buyer, buyingItem, callBack, player);
            buyCountMenu.open();

        } catch (Exception e) {
            Main.getMessage().sendMsg(player, Lang.getMessages("something_went_wrong"));
            Main.getMessage().error(e);
        }
    }

    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (true) {
            if (sb.indexOf("{amount}") != -1) {
                sb.replace(sb.indexOf("{amount}"), sb.indexOf("{amount}") + "{amount}".length(), String.valueOf(count));
                continue;
            }
            if (sb.indexOf("{buyer_name}") != -1) {
                sb.replace(sb.indexOf("{buyer_name}"), sb.indexOf("{buyer_name}") + "{buyer_name}".length(), player.getName());
                continue;
            }
            if (sb.indexOf("{price}") != -1) {
                sb.replace(sb.indexOf("{price}"), sb.indexOf("{price}") + "{price}".length(), NumberUtil.format(buyingItem.getPriceForOne() * count));
                continue;
            }
            break;
        }
        return buyingItem.replace(sb.toString());
    }

}
