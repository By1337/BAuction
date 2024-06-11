package org.by1337.bauction.action;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.db.event.BuyItemEvent;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.event.Event;
import org.by1337.bauction.event.EventType;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.network.impl.PacketSendMessage;
import org.by1337.bauction.util.PlayerUtil;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
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
            registerPlaceholders((CSellItem) buyingItem);
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
            registerPlaceholders((CSellItem) buyingItem);
    }

    public void run() {
        if (buyingItem != null) {
            Player player = menu.getPlayer();
            if (Main.getEcon().getBalance(player) < buyingItem.getPrice()) {
                Main.getMessage().sendMsg(player, Lang.getMessage("insufficient_balance"));
                return;
            }
            BuyItemEvent event = new BuyItemEvent(buyer, buyingItem);
            Main.getStorage().validateAndRemoveItem(event);

            OfflinePlayer seller = Bukkit.getOfflinePlayer(buyingItem.getSellerUuid());
            if (event.isValid()) {
                Main.getEcon().withdrawPlayer(player, buyingItem.getPrice());
                if (!buyingItem.getServer().equals(Main.getServerId()) && Main.getStorage() instanceof MysqlDb mysqlDb) {
                    mysqlDb.getMoneyGiver().give(buyingItem.getPrice(), buyingItem.getSellerUuid(), buyingItem.getServer());
                } else {
                    Main.getEcon().depositPlayer(seller, buyingItem.getPrice());
                }
                if (seller.isOnline()) {
                    Main.getMessage().sendMsg(seller.getPlayer(),
                            replace(Lang.getMessage("item_sold_to_buyer")));
                    Event event1 = new Event(seller.getPlayer(), EventType.BUY_ITEM_TO_SELLER, new BiPlaceholder(buyingItem, buyer));
                    Main.getEventManager().onEvent(event1);
                } else if (Main.getStorage() instanceof MysqlDb mysqlDb) {
                    mysqlDb.getPacketConnection().saveSend(new PacketSendMessage(
                            replace(Lang.getMessage("item_sold_to_buyer")), buyingItem.getSellerUuid()
                    ));
                }
                Main.getMessage().sendMsg(player, replace(Lang.getMessage("successful_purchase")));
                PlayerUtil.giveItems(player, buyingItem.getItemStack());
                Event event1 = new Event(player, EventType.BUY_ITEM, new BiPlaceholder(buyingItem, buyer));
                Main.getEventManager().onEvent(event1);
            } else {
                Main.getMessage().sendMsg(player, String.valueOf(event.getReason()));
            }
        } else {
            Main.getMessage().sendMsg(menu.getViewer(), replace(Lang.getMessage("something_went_wrong")));
        }
        menu.refresh();
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
