package org.by1337.bauction.config.adapter;

import org.bukkit.configuration.ConfigurationSection;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.adapter.ClassAdapter;
import org.by1337.blib.util.NameKey;
import org.by1337.bauction.boost.Boost;
import org.by1337.bauction.util.common.NumberUtil;

public class AdapterBoost implements ClassAdapter<Boost> {
    @Override
    public ConfigurationSection serialize(Boost boost, YamlContext yamlContext) {
        yamlContext.set("name", boost.getId());
        yamlContext.set("permission", boost.getPermission());
        yamlContext.set("external-slots", boost.getExternalSlots());
        yamlContext.set("external-sell-time", boost.getExternalSellTime());
        return yamlContext.getHandle();
    }

    @Override
    public Boost deserialize(YamlContext yamlContext) {
        NameKey id = yamlContext.getAsNameKey("name");
        String permission = yamlContext.getAsString("permission");
        int externalSlots = yamlContext.getAsInteger("external-slots");
        long externalSellTime = NumberUtil.getTime(yamlContext.getAsString("external-sell-time"));
        return new Boost(id, permission, externalSlots, externalSellTime);
    }
}
