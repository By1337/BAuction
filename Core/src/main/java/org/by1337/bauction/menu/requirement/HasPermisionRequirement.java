package org.by1337.bauction.menu.requirement;

import org.bukkit.configuration.file.YamlConfiguration;
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

    public CompoundTag saveAsNbt() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("permission", permission);
        compoundTag.putString("type", not ? RequirementType.MATH.notId :  RequirementType.MATH.id);
        return compoundTag;
    }

    public YamlContext saveAsYaml() {
        YamlContext yamlContext = new YamlContext(new YamlConfiguration());
        yamlContext.set("permission", permission);
        yamlContext.set("type", not ? RequirementType.MATH.notId :  RequirementType.MATH.id);
        return yamlContext;
    }

    @Override
    public RequirementType getType() {
        return RequirementType.MATH;
    }
}
