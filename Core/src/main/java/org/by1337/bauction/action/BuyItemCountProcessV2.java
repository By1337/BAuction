package org.by1337.bauction.action;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.db.event.BuyItemCountEvent;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.event.Event;
import org.by1337.bauction.event.EventType;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.network.impl.PacketSendMessage;
import org.by1337.bauction.util.PlayerUtil;
import org.by1337.blib.chat.placeholder.MultiPlaceholder;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public class BuyItemCountProcessV2 extends Placeholder {
    private final Menu menu;
    private final User buyer;
    private @Nullable final SellItem buyingItem;
    private final int count;

    public BuyItemCountProcessV2(Menu menu, @Nullable SellItem buyingItem, int count) {
        this.menu = menu;
        this.buyer = Main.getStorage().getUserOrCreate(menu.getPlayer());
        this.buyingItem = buyingItem;
        this.count = count;

        registerPlaceholder("{buyer_name}", buyer::getNickName);
        registerPlaceholder("{count}", () -> count);
        if (buyingItem != null)
            registerPlaceholders((CSellItem) buyingItem);
    }
    public void run(){
        if (buyingItem == null){
            Main.getMessage().sendMsg(menu.getViewer(), replace(Lang.getMessage("something_went_wrong")));
            return;
        }
        Player player = menu.getViewer();
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
                Event event1 = new Event(seller.getPlayer(), EventType.BUY_ITEM_COUNT_SELLER, new MultiPlaceholder(buyer, buyingItem, this));
                Main.getEventManager().onEvent(event1);
            } else if (Main.getStorage() instanceof MysqlDb mysqlDb) {
                mysqlDb.getPacketConnection().saveSend(new PacketSendMessage(
                        replace(Lang.getMessage("item_sold_to_buyer")), buyingItem.getSellerUuid()
                ));
            }

            Main.getMessage().sendMsg(player, replace(Lang.getMessage("successful_purchase")));
            ItemStack itemStack = buyingItem.getItemStack();
            itemStack.setAmount(count);
            PlayerUtil.giveItems(player, itemStack);
            Event event1 = new Event(player, EventType.BUY_ITEM_COUNT, new MultiPlaceholder(buyer, buyingItem, this));
            Main.getEventManager().onEvent(event1);
        } else {
            Main.getMessage().sendMsg(player, String.valueOf(event.getReason()));
        }
    }

}
