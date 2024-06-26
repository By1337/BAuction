package org.by1337.bauction.command.impl;

import org.bukkit.command.CommandSender;
import org.by1337.bauction.Main;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.time.TimeCounter;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.requires.RequiresPermission;


public class ReloadCmd extends Command<CommandSender> {
    public ReloadCmd(String command) {
        super(command);
        requires(new RequiresPermission<>("bauc.reload"));
        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args){
        TimeCounter timeCounter = new TimeCounter();
        Main plugin = (Main) Main.getInstance();
        plugin.fullReload();
        Main.getMessage().sendMsg(sender, Lang.getMessage("plugin_reload"), timeCounter.getTime());
    }
}
