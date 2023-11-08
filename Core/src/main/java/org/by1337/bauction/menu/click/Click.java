package org.by1337.bauction.menu.click;


import org.by1337.api.chat.Placeholderable;
import org.jetbrains.annotations.Nullable;

import org.by1337.bauction.menu.requirement.Requirements;
import org.by1337.bauction.menu.Menu;

public class Click implements IClick{
    private final String[] commands;
    private final Requirements requirements;
    private final ClickType clickType;

    public Click(String[] commands, @Nullable Requirements requirements, ClickType clickType) {
        this.clickType = clickType;
        this.commands = commands;
        this.requirements = requirements;
    }

    @Override
    public ClickType getClickType() {
        return clickType;
    }

    @Override
    public void run(Menu menu, Placeholderable holder) {
        if (requirements == null || requirements.check(holder, menu)){
            menu.runCommand(holder, commands);
        }else {
            requirements.runDenyCommands(menu, holder);
        }
    }
}
