package org.by1337.bauction.command.impl;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.time.TimeCounter;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentInteger;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.argument.ArgumentStrings;
import org.by1337.blib.command.requires.RequiresPermission;


public class RunCmd extends Command<CommandSender> {
    public RunCmd(String command) {
        super(command);
        requires((s) -> s instanceof Player);
        requires(new RequiresPermission<>("bauc.admin.debug.run"));
        argument(new ArgumentInteger<>("count"));
        argument(new ArgumentStrings<>("command"));

        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
        Player player = (Player) sender;
        ItemStack itemStack = player.getInventory().getItemInMainHand().clone();
        if (itemStack.getType().isAir()) {
            throw new CommandException(Lang.getMessage("item_in_hand_required"));
        }
        int count = (int) args.getOrThrow("count");
        String cmd = (String) args.getOrThrow("command");
        TimeCounter timeCounter = new TimeCounter();
        for (int i = 0; i < count; i++) {
            player.performCommand(cmd);
            player.getInventory().setItemInMainHand(itemStack);
        }
        Main.getMessage().sendMsg(sender, "&fThe '%s' command was executed %s times for %s ms.", cmd, count, timeCounter.getTime());

    }
}
