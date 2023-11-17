package org.by1337.util;

import net.minecraft.server.v1_16_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R3.persistence.CraftPersistentDataContainer;
import org.bukkit.persistence.PersistentDataContainer;

import java.util.ArrayList;
import java.util.List;

public class ParsePDCTags {
    public static List<String> parseTags(PersistentDataContainer persistentDataContainer){

        CraftPersistentDataContainer container = (CraftPersistentDataContainer) persistentDataContainer;

        NBTTagCompound compound = container.toTagCompound();

        List<String> list = new ArrayList<>();

        for (String key : compound.getKeys()) {
            list.add(key + ":" + compound.get(key).asString());
        }
        return list;
    }
}
