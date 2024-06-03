package org.by1337.bauction.lang;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.bauction.assets.AssetsManager;
import org.by1337.blib.configuration.YamlContext;

import java.io.File;
import java.util.HashMap;

public class Lang {
    private static final HashMap<String, String> messages = new HashMap<>();
    private static AssetsManager assetsManager;

    public static void load(Plugin plugin, AssetsManager assetsManager) {
        messages.clear();
        Lang.assetsManager = assetsManager;
        YamlContext context;
        File file;
        file = new File(plugin.getDataFolder().getPath() + "/message.yml");
        if (!file.exists()) {
            plugin.saveResource("message.yml", true);
        }
        context = new YamlContext(YamlConfiguration.loadConfiguration(file));

        messages.putAll(context.getMap("messages", String.class));
    }

    public static String getMessage(String s) {
        String str = messages.getOrDefault(s, assetsManager.getItemNames().getTranslation().get(s));
        if (str == null) return "missing message '" + s + "' in message.yml!";
        return str;
    }
}
