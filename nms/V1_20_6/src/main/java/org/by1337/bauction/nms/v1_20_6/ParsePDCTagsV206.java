package org.by1337.bauction.nms.v1_20_6;

import net.minecraft.nbt.CompoundTag;
import org.bukkit.craftbukkit.v1_20_R4.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;
import org.by1337.bauction.api.ParsePDCTags;

import java.util.ArrayList;
import java.util.List;

public class ParsePDCTagsV206 implements ParsePDCTags {
    @Override
    public List<String> parseTags(PersistentDataContainer persistentDataContainer) {
        CraftPersistentDataContainer container = (CraftPersistentDataContainer) persistentDataContainer;

        CompoundTag compound = container.toTagCompound();

        List<String> list = new ArrayList<>();

        for (String key : compound.getAllKeys()) {
            list.add(key + ":" + compound.get(key).getAsString());
        }
        return list;
    }
}
