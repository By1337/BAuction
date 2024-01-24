package org.by1337.bauction.menu.requirement;

import org.by1337.blib.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.Menu;

import java.util.List;

public class Requirements {
    private final List<IRequirement> requirements;
    private final String[] denyCommands;

    public Requirements(List<IRequirement> requirements, String[] denyCommands) {
        this.requirements = requirements;
        this.denyCommands = denyCommands;
    }

    public boolean check(Placeholderable holder, Menu menu){
        for (IRequirement iRequirement : requirements){
            try {
                if (!iRequirement.check(holder, menu)){
                    return false;
                }
            }catch (Exception e){
                Main.getMessage().error(e);
            }
        }
        return true;
    }
    public void runDenyCommands(Menu menu, Placeholderable holder){
        menu.runCommand(holder, denyCommands);
    }
}