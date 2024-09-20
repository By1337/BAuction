package org.by1337.bauction.action;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.PluginUnsoldItem;
import org.by1337.bauction.db.kernel.PluginUser;
import org.by1337.bauction.db.kernel.event.TakeUnsoldItemEvent;
import org.by1337.bauction.event.Event;
import org.by1337.bauction.event.EventType;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.player.PlayerUtil;
import org.by1337.blib.chat.placeholder.BiPlaceholder;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.bmenu.menu.Menu;
import org.jetbrains.annotations.Nullable;

public class TakeUnsoldItemProcess extends Placeholder {
    private final Menu menu;
    private final PluginUser taker;
    private @Nullable
    final PluginUnsoldItem takingItem;

    public TakeUnsoldItemProcess(Menu menu, PluginUser taker, @Nullable PluginUnsoldItem takingItem) {
        this.menu = menu;
        this.taker = taker;
        this.takingItem = takingItem;
        if (takingItem != null) {
            registerPlaceholders((Placeholder) takingItem);
        }
    }

    public TakeUnsoldItemProcess(Menu menu) {
        this.menu = menu;
        this.taker = Main.getStorage().getUserOrCreate(menu.getPlayer());
        if (menu.getLastClickedItem() != null && menu.getLastClickedItem().getData() instanceof PluginUnsoldItem) {
            takingItem = (PluginUnsoldItem) menu.getLastClickedItem().getData();
        } else {
            Main.getMessage().error("isn't sell item! Last clicked item='%s'", menu.getLastClickedItem());
            takingItem = null;
        }
        if (takingItem != null)
            registerPlaceholders((Placeholder) takingItem);
    }

    public void run() {
        if (takingItem != null) {
            Player player = menu.getViewer();

            TakeUnsoldItemEvent event = new TakeUnsoldItemEvent(taker, takingItem);
            Main.getStorage().onEvent(event).thenAccept(e -> {
                if (e.isValid()) {
                    PlayerUtil.giveItems(player, takingItem.getItemStack());
                    Event event1 = new Event(player, EventType.TAKE_UNSOLD_ITEM, new BiPlaceholder(taker, takingItem));
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
        return "TakeUnsoldItemProcess{" +
               "menu=" + menu +
               ", taker=" + taker +
               ", takingItem=" + takingItem +
               '}';
    }
}
