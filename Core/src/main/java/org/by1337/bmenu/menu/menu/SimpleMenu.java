package org.by1337.bmenu.menu.menu;

import org.bukkit.entity.Player;
import org.by1337.blib.command.CommandException;
import org.by1337.bmenu.menu.Menu;
import org.by1337.bmenu.menu.MenuLoader;
import org.by1337.bmenu.menu.MenuSetting;
import org.jetbrains.annotations.Nullable;

public class SimpleMenu extends Menu {


    public SimpleMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, MenuLoader menuLoader) {
        super(setting, player, previousMenu, menuLoader);
    }

    public SimpleMenu(MenuSetting setting, Player player, @Nullable Menu previousMenu, boolean async, MenuLoader menuLoader) {
        super(setting, player, previousMenu, async, menuLoader);
    }

    @Override
    protected void generate() {

    }

    @Override
    protected boolean runCommand(String[] cmd) throws CommandException {
        return false;
    }
}
