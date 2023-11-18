package org.by1337.bauction;

import org.bukkit.persistence.PersistentDataContainer;

import java.util.List;

public interface ParsePDCTags {
    List<String> parseTags(PersistentDataContainer persistentDataContainer);
}
