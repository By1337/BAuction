package org.by1337.bauction.action;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.db.event.BuyItemCountEvent;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.impl.BuyCountMenu;
import org.by1337.bauction.menu.impl.CallBack;
import org.by1337.bauction.menu.impl.ConfirmMenu;
import org.by1337.bauction.network.impl.PacketSendMessage;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.PlayerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class BuyItemCountProcess implements CallBack<Optional<ConfirmMenu.Result>> {

    private final SellItem buyingItem;
    private final User buyer;
    private final Player player;
    private final Menu menu;
    private final boolean fast;

    public BuyItemCountProcess(@NotNull SellItem buyingItem, @NotNull User buyer, Player player, Menu menu, boolean fast) {
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
            callBack = result -> {
                if (result.isPresent()) {
                    count = result.get();
                    if (fast) {
                        result(Optional.of(ConfirmMenu.Result.ACCEPT));
                    } else {
                        ItemStack itemStack = buyingItem.getItemStack();
                        itemStack.setAmount(count);
                        new ConfirmMenu(this, itemStack, player).open();
                    }
                }
            };

            buyCountMenu = new BuyCountMenu(buyer, buyingItem, callBack, player);
            buyCountMenu.open();

        } catch (Exception e) {
            Main.getMessage().sendMsg(player, Lang.getMessage("something_went_wrong"));
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

    @Override
    public void result(Optional<ConfirmMenu.Result> result) {
        if (result.isPresent()) {
            if (result.get() == ConfirmMenu.Result.ACCEPT) {
                double price = (buyingItem.getPriceForOne() * count);
                if (Main.getEcon().getBalance(player) < price) {
                    Main.getMessage().sendMsg(player, Lang.getMessage("insufficient_balance"));
                    menu.reopen();
                    return;
                }
                BuyItemCountEvent event = new BuyItemCountEvent(buyer, buyingItem, count);
                Main.getStorage().validateAndRemoveItem(event);

                OfflinePlayer seller = Bukkit.getOfflinePlayer(buyingItem.getSellerUuid());

                if (event.isValid()) {
                    Main.getEcon().withdrawPlayer(player, price);
                    if (!buyingItem.getServer().equals(Main.getServerId()) && Main.getStorage() instanceof MysqlDb mysqlDb) {
                        mysqlDb.getMoneyGiver().give(price, buyingItem.getSellerUuid(), buyingItem.getServer());
                    } else {
                        Main.getEcon().depositPlayer(seller, price);
                    }
                    if (seller.isOnline()) {
                        Main.getMessage().sendMsg(seller.getPlayer(),
                                replace(Lang.getMessage("item_sold_to_buyer")));
                    } else if (Main.getStorage() instanceof MysqlDb mysqlDb) {
                        mysqlDb.getPacketConnection().saveSend(new PacketSendMessage(
                                replace(Lang.getMessage("item_sold_to_buyer")), buyingItem.getSellerUuid()
                        ));
                    }

                    Main.getMessage().sendMsg(player, replace(Lang.getMessage("successful_purchase")));
                    ItemStack itemStack = buyingItem.getItemStack();
                    itemStack.setAmount(count);
                    PlayerUtil.giveItems(player, itemStack);
                } else {
                    Main.getMessage().sendMsg(player, String.valueOf(event.getReason()));
                }
                menu.reopen();
                return;
            }
            buyCountMenu.reopen();
        }
    }
}
