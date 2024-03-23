package org.by1337.bauction.menu.requirement;

import org.bukkit.entity.Player;
import org.by1337.bauction.util.ImmutableArrayList;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.bauction.Main;

import java.util.List;

public class Requirements {
    private final ImmutableArrayList<Requirement> requirements;
    private final ImmutableArrayList<String> denyCommands;

    public Requirements(List<Requirement> requirements, List<String> denyCommands) {
        this.requirements = new ImmutableArrayList<>(requirements);
        this.denyCommands = new ImmutableArrayList<>(denyCommands);
    }

    public boolean check(Placeholderable placeholderable, Player clicker) {
        for (Requirement requirement : requirements) {
            try {
                if (!requirement.test(placeholderable, clicker)) {
                    return false;
                }
            } catch (Exception e) {
                Main.getMessage().error(e);
            }
        }
        return true;
    }

    public ImmutableArrayList<Requirement> getRequirements() {
        return requirements;
    }

    public ImmutableArrayList<String> getDenyCommands() {
        return denyCommands;
    }

}