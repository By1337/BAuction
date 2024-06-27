package org.by1337.bauction.command.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.lang.Lang;
import org.by1337.blib.BLib;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.requires.RequiresPermission;

import java.util.Base64;

public class ParseNbtCmd extends Command<CommandSender> {
    public ParseNbtCmd(String command) {
        super(command);
        requires(new RequiresPermission<>("bauc.parse.nbt"));
        requires(sender -> sender instanceof Player);
        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
        Player player = (Player) sender;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType().isAir()) {
            throw new CommandException(Lang.getMessage("item_in_hand_required"));
        }

        String nbt = new String(Base64.getDecoder().decode(BLib.getApi().getItemStackSerialize().serialize(itemStack)));
        Main.getMessage().sendMsg(sender, nbt);
        Main.getMessage().logger(nbt);
    }
}
