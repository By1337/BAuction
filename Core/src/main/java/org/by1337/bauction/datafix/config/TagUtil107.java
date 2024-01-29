package org.by1337.bauction.datafix.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.by1337.bauction.Main;
import org.by1337.blib.configuration.YamlConfig;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class TagUtil107 {

    public void update() throws IOException, InvalidConfigurationException {
        File file = new File(Main.getInstance().getDataFolder().getPath() + "/tagUtil.yml");
        if (!file.exists()) {
            Main.getInstance().saveResource("tagUtil.yml", true);
        }
        YamlConfig config = new YamlConfig(file);

        Map<String, String> map = config.getContext().getMap("translate", String.class);
        Map<String, String> map1 = new HashMap<>();

        map.forEach((s, s1) -> map1.put(s1, s));

        config.getContext().set("translate", map1);
        config.trySave();
    }
}
