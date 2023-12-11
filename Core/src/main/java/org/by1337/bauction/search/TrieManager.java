package org.by1337.bauction.search;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.api.configuration.YamlContext;

import java.io.File;
import java.util.Map;

public class TrieManager {
    private Trie trie = new Trie();

    public TrieManager(Plugin plugin) {
        YamlContext context;
        File file;

        file = new File(plugin.getDataFolder().getPath() + "/tagUtil.yml");
        if (!file.exists()) {
            plugin.saveResource("tagUtil.yml", true);
        }
        context = new YamlContext(YamlConfiguration.loadConfiguration(file));

        Map<String, String> map = context.getMap("translate", String.class);

        for (Map.Entry<String, String> entry : map.entrySet()) {
            trie.insert(entry.getKey(), entry.getValue());
        }

    }

    public Trie getTrie() {
        return trie;
    }
}
