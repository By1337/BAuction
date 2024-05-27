package org.by1337.bmenu.menu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.nbt.impl.CompoundTag;
public class StringContainsRequirement implements Requirement {
    private final String input;
    private final String output;
    private final boolean not;

    public StringContainsRequirement(String input, String output, boolean not) {
        this.input = input;
        this.output = output;
        this.not = not;
    }

    public StringContainsRequirement(YamlContext context) {
        input = context.getAsString("input");
        output = context.getAsString("output");
        not = context.getAsString("type").startsWith("!");
    }

    public StringContainsRequirement(CompoundTag nbt) {
        input = nbt.getAsString("input");
        output = nbt.getAsString("output");
        not = nbt.getAsString("type").startsWith("!");
    }

    @Override
    public boolean test(Placeholderable placeholderable, Player clicker) {
        boolean b = placeholderable.replace(input).contains(placeholderable.replace(output));
        return not ? !b : b;
    }

    @Override
    public RequirementType getType() {
        return RequirementType.STRING_CONTAINS;
    }
}
