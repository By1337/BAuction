package org.by1337.bauction.search;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.bauction.assets.AssetsManager;
import org.by1337.blib.configuration.YamlContext;

import java.io.File;
import java.util.Map;

public class TrieManager {
    private Trie trie;

    public TrieManager(Plugin plugin, AssetsManager assetsManager) {
        load(plugin, assetsManager);
    }


    public void load(Plugin plugin, AssetsManager assetsManager) {
        trie = new Trie();
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

        for (Map.Entry<String, String> entry : assetsManager.getItemNames().getTranslationTabCompleterStyle().entrySet()) {
            trie.insert(entry.getValue(), entry.getKey());
        }
    }

    public Trie getTrie() {
        return trie;
    }
}
