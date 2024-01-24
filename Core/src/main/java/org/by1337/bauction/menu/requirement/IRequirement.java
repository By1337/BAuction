package org.by1337.bauction.menu.requirement;

import org.by1337.blib.chat.Placeholderable;
import org.by1337.bauction.menu.Menu;

public interface IRequirement {
    boolean check(Placeholderable holder, Menu menu);
    RequirementType getType();
}