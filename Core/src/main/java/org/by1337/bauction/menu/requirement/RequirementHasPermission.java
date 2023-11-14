package org.by1337.bauction.menu.requirement;

import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.menu.Menu;

public class RequirementHasPermission implements IRequirement {
    private final String permission;

    public RequirementHasPermission(String permission) {
        this.permission = permission;
    }

    @Override
    public boolean check(Placeholderable holder, Menu menu) {
        if (menu.getPlayer() != null) {
            return menu.getPlayer().hasPermission(holder.replace(permission));
        } else {
            throw new IllegalArgumentException("Player is null!: " + this);
        }
    }

    @Override
    public RequirementType getType() {
        return RequirementType.HAS_PERMISSION;
    }
}