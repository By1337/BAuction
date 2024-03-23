package org.by1337.bauction.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.by1337.bauction.menu.requirement.Requirements;
import org.by1337.bauction.util.OptionParser;
import org.by1337.blib.command.CommandException;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SimpleMenu extends Menu {


    public SimpleMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, OptionParser optionParser) {
        super(setting, player, previousMenu, optionParser);
    }

    public SimpleMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, boolean async, OptionParser optionParser) {
        super(setting, player, previousMenu, async, optionParser);
    }

    @Override
    protected void generate() {

    }

    @Override
    protected boolean runCommand(String[] cmd) throws CommandException {
        return false;
    }
}
