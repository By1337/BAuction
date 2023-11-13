package org.by1337.bauction.config.adapter;

import org.bukkit.configuration.ConfigurationSection;
import org.by1337.api.configuration.YamlContext;
import org.by1337.api.configuration.adapter.ClassAdapter;
import org.by1337.bauction.menu.requirement.IRequirement;
import org.by1337.bauction.menu.requirement.RequirementHasPermission;
import org.by1337.bauction.menu.requirement.RequirementStringContains;
import org.by1337.bauction.menu.requirement.RequirementStringEquals;

import java.util.Objects;

public class AdapterIRequirement implements ClassAdapter<IRequirement> {
    @Override
    public ConfigurationSection serialize(IRequirement obj, YamlContext context) {
        throw new IllegalStateException();
    }

    @Override
    public IRequirement deserialize(YamlContext context) {
        String type = context.getAsString("type");
        if (type.equalsIgnoreCase("string equals") || type.equalsIgnoreCase("sq")) {
            String input = context.getAsString("input");
            String output = context.getAsString("output");
            return new RequirementStringEquals(input, output);
        }
        if (type.equalsIgnoreCase("string contains") || type.equalsIgnoreCase("sc")) {
            String input = context.getAsString("input");
            String input2 = context.getAsString("input2");
            String output = context.getAsString("output");
            return  new RequirementStringContains(input, input2, output);
        }
        if (type.equalsIgnoreCase("has permission") || type.equalsIgnoreCase("hp")) {
            return new RequirementHasPermission(context.getAsString("permission"));
        }
        throw new IllegalArgumentException("unknown IRequirement type: " + type);
    }
}
