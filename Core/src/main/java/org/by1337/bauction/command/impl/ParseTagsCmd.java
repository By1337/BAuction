package org.by1337.bauction.command.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.auction.TagUtil;
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
import java.util.List;

public class ParseTagsCmd extends Command<CommandSender> {
    public ParseTagsCmd(String command) {
        super(command);
        requires(new RequiresPermission<>("bauc.parse.tags"));
        requires(sender -> sender instanceof Player);
        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
        Player player = (Player) sender;
        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType().isAir()) {
            throw new CommandException(Lang.getMessage("item_in_hand_required"));
        }
        ComponentBuilder builder = new ComponentBuilder();
        List<String> list = new ArrayList<>(TagUtil.getTags(itemStack));
        for (int i = 0; i < list.size(); i++) {
            String tag = list.get(i);
            builder.component(new Component(tag)
                    .hoverEvent(new HoverEvent(new HoverEventContentsString("click to copy")))
                    .clickEvent(new ClickEvent(ClickEventType.COPY_TO_CLIPBOARD, tag))
            );
            if (i != list.size() - 1) {
                builder.component(new Component(", "));
            }
        }
        Main.getMessage().sendRawMsg(sender, builder);
    }
}
