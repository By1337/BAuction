package org.by1337.bauction.action;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.UnsoldItem;
import org.by1337.bauction.auc.User;
import org.by1337.bauction.db.event.TakeUnsoldItemEvent;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.impl.CallBack;
import org.by1337.bauction.menu.impl.ConfirmMenu;
import org.by1337.bauction.util.PlayerUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TakeUnsoldItemProcess {
    private final UnsoldItem takingItem;
    private final User taker;
    private final Menu menu;
    private final Player player;
    private final boolean fast;

    public TakeUnsoldItemProcess(@NotNull UnsoldItem takingItem, @NotNull User taker, Menu menu, Player player, boolean fast) {
        this.takingItem = takingItem;
        this.taker = taker;
        this.menu = menu;
        this.player = player;
        this.fast = fast;
    }

    public void process() {
        CallBack<Optional<ConfirmMenu.Result>> callBack = result -> {
            if (result.isPresent()) {
                if (result.get() == ConfirmMenu.Result.ACCEPT) {
                    TakeUnsoldItemEvent event = new TakeUnsoldItemEvent(taker, takingItem);
                    Main.getStorage().validateAndRemoveItem(event);

                    if (event.isValid()) {
                        Main.getMessage().sendMsg(player, takingItem.replace(Lang.getMessage("successful_item_retrieval")));
                        PlayerUtil.giveItems(player, takingItem.getItemStack());
                    } else {
                        Main.getMessage().sendMsg(player, String.valueOf(event.getReason()));
                    }
                }
            }
            menu.reopen();
        };
        if (fast){
            callBack.result(Optional.of(ConfirmMenu.Result.ACCEPT));
        }else {
            ConfirmMenu confirmMenu = new ConfirmMenu(callBack, takingItem.getItemStack(), player);
            confirmMenu.registerPlaceholderable(taker);
            confirmMenu.registerPlaceholderable(takingItem);
            confirmMenu.open();
        }

    }
}
