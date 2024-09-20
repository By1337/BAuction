package org.by1337.bauction.action;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.PluginUser;
import org.by1337.bauction.db.kernel.event.BuyCountItemEvent;
import org.by1337.bauction.event.Event;
import org.by1337.bauction.event.EventType;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.common.NumberUtil;
import org.by1337.bauction.util.player.PlayerUtil;
import org.by1337.blib.chat.placeholder.MultiPlaceholder;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public class BuyItemCountProcess extends Placeholder {
    private final Menu menu;
    private final PluginUser buyer;
    private @Nullable final PluginSellItem buyingItem;
    private final int count;

    public BuyItemCountProcess(Menu menu, @Nullable PluginSellItem buyingItem, int count) {
        this.menu = menu;
        this.buyer = Main.getStorage().getUserOrCreate(menu.getPlayer());
        this.buyingItem = buyingItem;
        this.count = count;


        if (buyingItem != null) {
            registerPlaceholders( buyingItem);
            registerPlaceholder("{price}", () -> NumberUtil.format(buyingItem.getPriceForOne() * count));
        }
        registerPlaceholder("{amount}", () -> count);
        registerPlaceholder("{buyer_name}", buyer::getNickName);
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
        BuyCountItemEvent event = new BuyCountItemEvent(buyer, buyingItem, count);

        Main.getStorage().onEvent(event).whenComplete((e, t) -> {
            OfflinePlayer seller = Bukkit.getOfflinePlayer(buyingItem.getSellerUuid());
            if (e.isValid()) {
                Main.getEcon().withdrawPlayer(player, price);
                if (/*!buyingItem.getServer().equals(Main.getServerUUID()) && Main.getStorage() instanceof MysqlDb mysqlDb*/ false) { // todo multi server econ
                    //mysqlDb.getMoneyGiver().give(price, buyingItem.getSellerUuid(), buyingItem.getServer());
                } else {
                    Main.getEcon().depositPlayer(seller, price);
                }
                if (seller.isOnline()) {
                    //Main.getMessage().sendMsg(seller.getPlayer(), replace(Lang.getMessage("item_sold_to_buyer")));
                    Event event1 = new Event(seller.getPlayer(), EventType.BUY_ITEM_COUNT_SELLER, new MultiPlaceholder(this, buyer, buyingItem));
                    Main.getEventManager().onEvent(event1);
                } else if (/*Main.getStorage() instanceof MysqlDb mysqlDb*/ false) { // todo multi server events
//                mysqlDb.getPacketConnection().saveSend(new PacketSendMessage(
//                        replace(Lang.getMessage("item_sold_to_buyer")), buyingItem.getSellerUuid() // todo
//                ));
                }

                //Main.getMessage().sendMsg(player, replace(Lang.getMessage("successful_purchase")));
                ItemStack itemStack = buyingItem.getItemStack();
                itemStack.setAmount(count);
                PlayerUtil.giveItems(player, itemStack);
                Event event1 = new Event(player, EventType.BUY_ITEM_COUNT, new MultiPlaceholder(this, buyer, buyingItem));
                Main.getEventManager().onEvent(event1);
            } else {
                Main.getMessage().sendMsg(player, event.getReason());
            }
        });


    }

}
