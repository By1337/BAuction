package org.by1337.bauction.datafix.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.by1337.api.configuration.YamlConfig;
import org.by1337.api.configuration.YamlContext;
import org.by1337.bauction.Main;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MessagesUpdater {
    public void update() throws IOException, InvalidConfigurationException {
        File messages = new File(Main.getInstance().getDataFolder() + "/message.yml");
        if (!messages.exists()) return;
        Map<String, String> map = getMessagesFromPlugin();
        YamlConfig file = new YamlConfig(messages);
        Map<String, String> map1 = file.getContext().getMap("messages", String.class, new HashMap<>());
        map.forEach((k, v) -> {
            if (!map1.containsKey(k)) {
                file.getContext().set("messages." + k, v);
            }
        });
        file.trySave();
    }

    public static Map<String, String> getMessagesFromPlugin() throws IOException {
        Map<String, String> map;
        try (InputStream in = Main.getInstance().getResource("message.yml")) {
            File tempFile = File.createTempFile("message", ".yml");

            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            YamlConfiguration yamlConfiguration = YamlConfiguration.loadConfiguration(tempFile);

            YamlContext context = new YamlContext(yamlConfiguration);

            map = context.getMap("messages", String.class, new HashMap<>());
            tempFile.delete();
        }
        return map;
    }
}
