package org.by1337.bauction.command.impl;


import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.by1337.bauction.Main;
import org.by1337.bauction.lang.Lang;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.argument.ArgumentString;
import org.by1337.blib.command.requires.RequiresPermission;

import java.util.List;

public class AddTagCmd extends Command<CommandSender> {
    public AddTagCmd(String command) {
        super(command);
        requires(new RequiresPermission<>("bauc.admin.addTag"));
        requires(sender -> sender instanceof Player);
        argument(new ArgumentString<>("key", List.of("[tag key]")));
        argument(new ArgumentString<>("value", List.of("[tag value]")));
        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
        Player player = (Player) sender;

        String key = (String) args.getOrThrow("key");
        String value = (String) args.getOrThrow("value");

        ItemStack itemStack = player.getInventory().getItemInMainHand();
        if (itemStack.getType().isAir()) {
            throw new CommandException(Lang.getMessage("item_in_hand_required"));
        }

        ItemMeta im = itemStack.getItemMeta();
        im.getPersistentDataContainer().set(NamespacedKey.fromString(key), PersistentDataType.STRING, value);
        itemStack.setItemMeta(im);
        Main.getMessage().sendMsg(sender, "&adone");
    }
}
