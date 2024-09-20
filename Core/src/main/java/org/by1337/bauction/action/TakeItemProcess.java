package org.by1337.bauction.action;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.PluginSellItem;
import org.by1337.bauction.db.kernel.PluginUser;

import org.by1337.bauction.db.kernel.event.TakeItemEvent;
import org.by1337.bauction.event.Event;
import org.by1337.bauction.event.EventType;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.player.PlayerUtil;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public class TakeItemProcess extends Placeholder {
    private final PluginSellItem takingItem;
    private final PluginUser taker;
    private final Menu menu;

    public TakeItemProcess(Menu menu, PluginUser taker, @Nullable PluginSellItem takingItem) {
        this.menu = menu;
        this.taker = taker;
        this.takingItem = takingItem;
        if (takingItem != null) {
            registerPlaceholder("{buyer_name}", taker::getNickName);
            registerPlaceholders((PluginSellItem) takingItem);
        }

    }

    public TakeItemProcess(Menu menu) {
        this.menu = menu;
        this.taker = Main.getStorage().getUserOrCreate(menu.getPlayer());
        if (menu.getLastClickedItem() != null && menu.getLastClickedItem().getData() instanceof PluginSellItem) {
            takingItem = (PluginSellItem) menu.getLastClickedItem().getData();
        } else {
            Main.getMessage().error("isn't sell item! Last clicked item='%s'", menu.getLastClickedItem());
            takingItem = null;
        }
        registerPlaceholder("{taker_name}", taker::getNickName);
        if (takingItem != null)
            registerPlaceholders((PluginSellItem) takingItem);
    }

    public void run() {
        if (takingItem != null) {
            Player player = menu.getViewer();
            TakeItemEvent event = new TakeItemEvent(takingItem, taker);
            Main.getStorage().onEvent(event).thenAccept(e -> {
                if (e.isValid()) {
                    PlayerUtil.giveItems(player, takingItem.getItemStack());
                    Event event1 = new Event(player, EventType.TAKE_ITEM, new BiPlaceholder(taker, takingItem));
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
}

