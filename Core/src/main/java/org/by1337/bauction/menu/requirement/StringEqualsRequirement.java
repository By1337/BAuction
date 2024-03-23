package org.by1337.bauction.menu.requirement;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.nbt.impl.CompoundTag;
public class StringEqualsRequirement implements Requirement {
    private final String input;
    private final String output;
    private final boolean not;

    public StringEqualsRequirement(YamlContext context) {
        input = context.getAsString("input");
        output = context.getAsString("output");
        not = context.getAsString("type").startsWith("!");
    }

    public StringEqualsRequirement(CompoundTag nbt) {
        input = nbt.getAsString("input");
        output = nbt.getAsString("output");
        not = nbt.getAsString("type").startsWith("!");
    }

    @Override
    public boolean test(Placeholderable placeholderable, Player clicker) {
        boolean b = placeholderable.replace(input).equals(placeholderable.replace(output));
        return not ? !b : b;
    }

    public CompoundTag saveAsNbt() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("input", input);
        compoundTag.putString("output", output);
        compoundTag.putString("type", not ? RequirementType.STRING_EQUALS.notId : RequirementType.STRING_EQUALS.id);
        return compoundTag;
    }

    public YamlContext saveAsYaml() {
        YamlContext yamlContext = new YamlContext(new YamlConfiguration());
        yamlContext.set("input", input);
        yamlContext.set("output", output);
        yamlContext.set("type", not ? RequirementType.STRING_EQUALS.notId : RequirementType.STRING_EQUALS.id);
        return yamlContext;
    }

    @Override
    public RequirementType getType() {
        return RequirementType.STRING_EQUALS;
    }
}
