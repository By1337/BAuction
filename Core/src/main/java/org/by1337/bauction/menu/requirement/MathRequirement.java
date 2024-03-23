package org.by1337.bauction.menu.requirement;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.math.MathParser;
import org.by1337.blib.nbt.impl.CompoundTag;
public class MathRequirement implements Requirement {
    private final String expression;
    private final boolean not;

    public MathRequirement(YamlContext context) {
        expression = context.getAsString("expression");
        not = context.getAsString("type").startsWith("!");
    }
    public MathRequirement(CompoundTag compoundTag) {
        expression = compoundTag.getAsString("expression");
        not = compoundTag.getAsString("type").startsWith("!");
    }

    @Override
    public boolean test(Placeholderable placeholderable, Player clicker) {
        var b = MathParser.mathSave(placeholderable.replace(expression)).equals("1");
        return not ? !b : b;
    }

    public CompoundTag saveAsNbt() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("expression", expression);
        compoundTag.putString("type", not ? RequirementType.MATH.notId :  RequirementType.MATH.id);
        return compoundTag;
    }

    public YamlContext saveAsYaml() {
        YamlContext yamlContext = new YamlContext(new YamlConfiguration());
        yamlContext.set("expression", expression);
        yamlContext.set("type", not ? RequirementType.MATH.notId :  RequirementType.MATH.id);
        return yamlContext;
    }

    @Override
    public RequirementType getType() {
        return RequirementType.MATH;
    }
}
