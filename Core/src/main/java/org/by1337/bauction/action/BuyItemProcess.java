package org.by1337.bauction.action;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.db.kernel.User;
import org.by1337.bauction.db.v2.BuyItemEvent;
import org.by1337.bauction.event.Event;
import org.by1337.bauction.event.EventType;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.player.PlayerUtil;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.blib.chat.placeholder.MultiPlaceholder;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public class BuyItemProcess extends Placeholder {
    private final Menu menu;
    private final User buyer;
    private @Nullable
    final SellItem buyingItem;

    public BuyItemProcess(Menu menu, User buyer, @Nullable SellItem buyingItem) {
        this.menu = menu;
        this.buyer = buyer;
        this.buyingItem = buyingItem;
        if (buyingItem != null) {
            registerPlaceholder("{buyer_name}", buyer::getNickName);
            registerPlaceholders((SellItem) buyingItem);
        }

    }

    public BuyItemProcess(Menu menu) {
        this.menu = menu;
        this.buyer = Main.getStorage().getUserOrCreate(menu.getPlayer());
        if (menu.getLastClickedItem() != null && menu.getLastClickedItem().getData() instanceof SellItem) {
            buyingItem = (SellItem) menu.getLastClickedItem().getData();
        } else {
            Main.getMessage().error("isn't sell item! Last clicked item='%s'", menu.getLastClickedItem());
            buyingItem = null;
        }
        registerPlaceholder("{buyer_name}", buyer::getNickName);
        if (buyingItem != null)
            registerPlaceholders((SellItem) buyingItem);
    }

    public void run() {
        if (buyingItem != null) {
            Player player = menu.getPlayer();
            if (Main.getEcon().getBalance(player) < buyingItem.getPrice()) {
                Main.getMessage().sendMsg(player, Lang.getMessage("insufficient_balance"));
                return;
            }
            BuyItemEvent event = new BuyItemEvent(buyer, buyingItem);
            Main.getStorage().onEvent(event).thenAccept(e -> {
                OfflinePlayer seller = Bukkit.getOfflinePlayer(buyingItem.getSellerUuid());
                if (event.isValid()) {
                    Main.getEcon().withdrawPlayer(player, buyingItem.getPrice());
                    if (/*!buyingItem.getServer().equals(Main.getServerId()) && Main.getStorage() instanceof MysqlDb mysqlDb*/ false) {// todo multi server econ
                      //  mysqlDb.getMoneyGiver().give(buyingItem.getPrice(), buyingItem.getSellerUuid(), buyingItem.getServer());
                    } else {
                        Main.getEcon().depositPlayer(seller, buyingItem.getPrice());
                    }
                    if (seller.isOnline()) {
                        //Main.getMessage().sendMsg(seller.getPlayer(), replace(Lang.getMessage("item_sold_to_buyer")));
                        Event event1 = new Event(seller.getPlayer(), EventType.BUY_ITEM_TO_SELLER, new MultiPlaceholder(buyingItem, buyer, this));
                        Main.getEventManager().onEvent(event1);
                    } else if (/*Main.getStorage() instanceof MysqlDb mysqlDb*/ false) {
//                    mysqlDb.getPacketConnection().saveSend(new PacketSendMessage( // todo multi server events
//                            replace(Lang.getMessage("item_sold_to_buyer")), buyingItem.getSellerUuid() // todo
//                    ));
                    }
                    // Main.getMessage().sendMsg(player, replace(Lang.getMessage("successful_purchase")));
                    PlayerUtil.giveItems(player, buyingItem.getItemStack());
                    Event event1 = new Event(player, EventType.BUY_ITEM, new BiPlaceholder(buyingItem, buyer));
                    Main.getEventManager().onEvent(event1);
                } else {
                    Main.getMessage().sendMsg(player, event.getReason());
                }
                menu.refresh();
            });


        } else {
            Main.getMessage().sendMsg(menu.getViewer(), replace(Lang.getMessage("something_went_wrong")));
            menu.refresh();
        }
    }

    @Override
    public String toString() {
        return "BuyItemProcess{" +
                "menu=" + menu +
                ", buyer=" + buyer +
                ", buyingItem=" + buyingItem +
                '}';
    }
}
