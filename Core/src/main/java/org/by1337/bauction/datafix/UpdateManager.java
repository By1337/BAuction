package org.by1337.bauction.datafix;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.api.configuration.YamlContext;
import org.by1337.bauction.Main;
import org.by1337.bauction.datafix.db.json.DBUpdateToV2;

import java.io.File;
import java.io.IOException;

public class UpdateManager {
    private final static int CURRENT_VERSION = 1;

    public static void checkUpdate() {
        Plugin plugin = Main.getInstance();
        YamlContext config;
        File configFile;
        configFile = new File(plugin.getDataFolder() + "/config.yml");
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", true);
        }
        config = new YamlContext(YamlConfiguration.loadConfiguration(configFile));

        int version = config.getAsInteger("version", 0);

        if (CURRENT_VERSION != version){
            Main.getMessage().warning("detected deprecated config!");
            Main.getMessage().logger("start update...");
        }
        run(version);

        config.set("version", CURRENT_VERSION);
        try {
            ((YamlConfiguration) config.getHandle()).save(configFile);
        } catch (IOException e) {
            Main.getMessage().error(e);
        }
    }
    private static void run(int version){
        if (version == 0){
            DBUpdateToV2 DBUpdateToV2 = new DBUpdateToV2();
            DBUpdateToV2.update();
            version++;
        }
    }
}
