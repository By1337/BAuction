package org.by1337.bauction.datafix.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.by1337.api.configuration.YamlConfig;
import org.by1337.bauction.Main;

import java.io.File;
import java.io.IOException;

public class DbCfg109 {

    public void run() throws IOException, InvalidConfigurationException {
        File file = new File(Main.getInstance().getDataFolder() + "/dbCfg.yml");
        if (file.exists()) {
            YamlConfig config = new YamlConfig(file);
            config.getContext().set("server-id", "server-1");
            config.trySave();
        }
        ((Main) Main.getInstance()).reloadDbCfg();
    }
}
