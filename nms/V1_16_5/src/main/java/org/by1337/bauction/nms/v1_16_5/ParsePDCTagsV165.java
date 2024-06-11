package org.by1337.bauction.nms.v1_16_5;

import net.minecraft.nbt.CompoundTag;

import org.bukkit.craftbukkit.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.by1337.bauction.api.ParsePDCTags;

import java.util.ArrayList;
import java.util.List;

public class ParsePDCTagsV165 implements ParsePDCTags {
    @Override
    public List<String> parseTags(PersistentDataContainer persistentDataContainer) {
        CraftPersistentDataContainer container = (CraftPersistentDataContainer) persistentDataContainer;

        CompoundTag compound = container.toTagCompound();

        List<String> list = new ArrayList<>();

        for (String key : compound.getKeys()) {
            list.add(key + ":" + compound.get(key).asString());
        }
        return list;
    }
}
