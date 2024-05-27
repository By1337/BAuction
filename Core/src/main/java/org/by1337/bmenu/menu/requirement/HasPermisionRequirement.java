package org.by1337.bmenu.menu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.nbt.impl.CompoundTag;

public class HasPermisionRequirement implements Requirement {
    private final String permission;
    private final boolean not;

    public HasPermisionRequirement(YamlContext context) {
        permission = context.getAsString("permission");
        not = context.getAsString("type").startsWith("!");
    }
    public HasPermisionRequirement(CompoundTag compoundTag) {
        permission = compoundTag.getAsString("permission");
        not = compoundTag.getAsString("type").startsWith("!");
    }

    @Override
    public boolean test(Placeholderable placeholderable, Player clicker) {
        return not ? !clicker.hasPermission(placeholderable.replace(permission)) : clicker.hasPermission(placeholderable.replace(permission));
    }


    @Override
    public RequirementType getType() {
        return RequirementType.MATH;
    }
}
