package org.by1337.bauction.config.adapter;

import org.bukkit.configuration.ConfigurationSection;
import org.by1337.api.configuration.YamlContext;
import org.by1337.api.configuration.adapter.ClassAdapter;
import org.by1337.bauction.menu.requirement.IRequirement;
import org.by1337.bauction.menu.requirement.Requirements;

import java.util.ArrayList;
import java.util.List;

public class AdapterRequirements implements ClassAdapter<Requirements> {
    @Override
    public ConfigurationSection serialize(Requirements obj, YamlContext context) {
        throw new IllegalStateException();
    }

    @Override
    public Requirements deserialize(YamlContext context) {
        List<IRequirement> requirementList = context.getList("requirements", IRequirement.class, new ArrayList<>());
        String[] denyCommands = context.getList("deny_commands", String.class, new ArrayList<>()).toArray(new String[0]);
        return new Requirements(requirementList, denyCommands);
    }
}
