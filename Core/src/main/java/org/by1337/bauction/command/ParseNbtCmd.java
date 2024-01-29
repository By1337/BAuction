package org.by1337.bauction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.TagUtil;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.ClickEvent;
import org.by1337.blib.chat.ClickEventType;
import org.by1337.blib.chat.Component;
import org.by1337.blib.chat.ComponentBuilder;
import org.by1337.blib.chat.hover.HoverEvent;
import org.by1337.blib.chat.hover.HoverEventContentsString;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.requires.RequiresPermission;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

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

    }
}
