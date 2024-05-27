package org.by1337.bmenu.menu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
public interface Requirement {
    boolean test(Placeholderable placeholderable, Player clicker);
    RequirementType getType();
}