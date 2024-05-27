package org.by1337.bmenu.config.adapter;

import org.bukkit.configuration.ConfigurationSection;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.adapter.ClassAdapter;
import org.by1337.bmenu.menu.requirement.Requirement;
import org.by1337.bmenu.menu.requirement.RequirementType;

public class AdapterIRequirement implements ClassAdapter<Requirement> {
    @Override
    public ConfigurationSection serialize(Requirement obj, YamlContext context) {
        throw new IllegalStateException();
    }

    @Override
    public Requirement deserialize(YamlContext context) {
        String type = context.getAsString("type");

        RequirementType requirementType = RequirementType.byName(type);
        if (requirementType == null) {
            throw new IllegalArgumentException("unknown requirement type: " + type);
        }
        return requirementType.fromYaml.apply(context);
    }
}
