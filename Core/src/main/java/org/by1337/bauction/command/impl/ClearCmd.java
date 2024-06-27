package org.by1337.bauction.command.impl;

import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.MysqlDb;
import org.by1337.bauction.lang.Lang;
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
        Main.getStorage().clear();
        Main.getMessage().sendMsg(sender, Lang.getMessage("auc-cleared"));
    }
}
