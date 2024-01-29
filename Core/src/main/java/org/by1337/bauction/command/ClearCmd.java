package org.by1337.bauction.command;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.impl.CallBack;
import org.by1337.bauction.menu.impl.ConfirmMenu;
import org.by1337.bauction.util.TimeCounter;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.requires.RequiresPermission;

import java.util.Optional;


public class ClearCmd extends Command<CommandSender> {
    public ClearCmd(String command) {
        super(command);
        requires(new RequiresPermission<>("bauc.admin.debug.clear"));
        requires(s -> !(Main.getStorage() instanceof MysqlDb));
        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
        CallBack<Optional<ConfirmMenu.Result>> callBack = (res) -> {
            if (res.isPresent()) {
                if (res.get() == ConfirmMenu.Result.ACCEPT) {
                    Main.getStorage().clear();
                    Main.getMessage().sendMsg(sender, Lang.getMessage("auc-cleared"));
                }
            }
            if (sender instanceof Player player)
                player.closeInventory();
        };

        if (sender instanceof Player player) {
            ItemStack itemStack = new ItemStack(Material.JIGSAW);
            ItemMeta im = itemStack.getItemMeta();
            im.setDisplayName(Main.getMessage().messageBuilder(Lang.getMessage("auc-clear-confirm")));
            itemStack.setItemMeta(im);
            ConfirmMenu menu = new ConfirmMenu(callBack, itemStack, player);
            menu.open();
        } else {
            callBack.result(Optional.of(ConfirmMenu.Result.ACCEPT));
        }
    }
}
