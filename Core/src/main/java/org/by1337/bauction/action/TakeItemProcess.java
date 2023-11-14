package org.by1337.bauction.action;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.MemorySellItem;
import org.by1337.bauction.db.MemoryUser;
import org.by1337.bauction.db.event.TakeItemEvent;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.impl.CallBack;
import org.by1337.bauction.menu.impl.ConfirmMenu;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class TakeItemProcess {
    private final MemorySellItem takingItem;
    private final MemoryUser taker;
    private final Menu menu;
    private final Player player;

    public TakeItemProcess(@NotNull MemorySellItem takingItem, @NotNull MemoryUser taker, Menu menu, Player player) {
        this.takingItem = takingItem;
        this.taker = taker;
        this.menu = menu;
        this.player = player;
    }

    public void process() {
        CallBack<Optional<ConfirmMenu.Result>> callBack = result -> {
            if (result.isPresent()) {
                if (result.get() == ConfirmMenu.Result.ACCEPT) {
                    TakeItemEvent event = new TakeItemEvent(taker, takingItem);
                    Main.getStorage().validateAndRemoveItem(event);

                    if (event.isValid()) {
                        Main.getMessage().sendMsg(player, "&aВы успешно забрали свой предмет!");
                        Menu.giveItems(player, takingItem.getItemStack()).forEach(i -> player.getLocation().getWorld().dropItem(player.getLocation(), i));
                    } else {
                        Main.getMessage().sendMsg(player, String.valueOf(event.getReason()));
                    }
                }
            }
            menu.reopen();
        };
        ConfirmMenu confirmMenu = new ConfirmMenu(callBack, takingItem.getItemStack(), player);
        confirmMenu.registerPlaceholderable(taker);
        confirmMenu.registerPlaceholderable(takingItem);
        confirmMenu.open();
    }
}
