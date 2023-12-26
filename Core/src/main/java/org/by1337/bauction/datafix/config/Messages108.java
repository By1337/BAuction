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

public class Messages108 {

    public void update() throws IOException, InvalidConfigurationException {
        File messages = new File(Main.getInstance().getDataFolder() + "/message.yml");
        if (!messages.exists()) return;

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

        YamlConfig file = new YamlConfig(messages);

        file.getContext().set("messages.count-req", map.get("messages.count-req"));
        file.getContext().set("messages.sale-by-the-piece-format-on", map.get("messages.sale-by-the-piece-format-on"));
        file.getContext().set("messages.sale-by-the-piece-format-off", map.get("messages.sale-by-the-piece-format-off"));
        file.getContext().set("messages.item-in-black-list", map.get("messages.item-in-black-list"));
        file.trySave();
    }
}
