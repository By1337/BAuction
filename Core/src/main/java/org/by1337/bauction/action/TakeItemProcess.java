package org.by1337.bauction.action;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.SellItem;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.db.event.TakeItemEvent;
import org.by1337.bauction.db.kernel.CSellItem;

import org.by1337.bauction.event.Event;
import org.by1337.bauction.event.EventType;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.PlayerUtil;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public class TakeItemProcess extends Placeholder {
    private final SellItem takingItem;
    private final User taker;
    private final Menu menu;

    public TakeItemProcess(Menu menu, User taker, @Nullable SellItem takingItem) {
        this.menu = menu;
        this.taker = taker;
        this.takingItem = takingItem;
        if (takingItem != null) {
            registerPlaceholder("{buyer_name}", taker::getNickName);
            registerPlaceholders((CSellItem) takingItem);
        }

    }

    public TakeItemProcess(Menu menu) {
        this.menu = menu;
        this.taker = Main.getStorage().getUserOrCreate(menu.getPlayer());
        if (menu.getLastClickedItem() != null && menu.getLastClickedItem().getData() instanceof SellItem) {
            takingItem = (SellItem) menu.getLastClickedItem().getData();
        } else {
            Main.getMessage().error("isn't sell item! Last clicked item='%s'", menu.getLastClickedItem());
            takingItem = null;
        }
        registerPlaceholder("{taker_name}", taker::getNickName);
        if (takingItem != null)
            registerPlaceholders((CSellItem) takingItem);
    }

    public void run() {
        if (takingItem != null) {
            Player player = menu.getViewer();

            TakeItemEvent event = new TakeItemEvent(taker, takingItem);
            Main.getStorage().validateAndRemoveItem(event);

            if (event.isValid()) {
              //  Main.getMessage().sendMsg(player, takingItem.replace(Lang.getMessage("successful_item_retrieval")));
                PlayerUtil.giveItems(player, takingItem.getItemStack());
                Event event1 = new Event(player, EventType.TAKE_ITEM, new BiPlaceholder(taker, takingItem));
                Main.getEventManager().onEvent(event1);
            } else {
                Main.getMessage().sendMsg(player, String.valueOf(event.getReason()));
            }
        } else {
            Main.getMessage().sendMsg(menu.getViewer(), replace(Lang.getMessage("something_went_wrong")));
        }
        menu.refresh();
    }
}

