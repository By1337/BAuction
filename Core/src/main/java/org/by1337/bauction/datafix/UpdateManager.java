package org.by1337.bauction.datafix;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.api.configuration.YamlContext;
import org.by1337.bauction.Main;
import org.by1337.bauction.datafix.config.*;
import org.by1337.bauction.datafix.db.DBUpdate107;
import org.by1337.bauction.datafix.db.DBUpdate108;
import org.by1337.bauction.datafix.db.DBUpdate109;
import org.by1337.bauction.datafix.db.mysql.MySqlDBUpdater1;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class UpdateManager {
    private final static int CURRENT_VERSION = 5;

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

        if (CURRENT_VERSION != version) {
            Main.getMessage().warning("detected deprecated config!");
            Main.getMessage().logger("start update...");
        }

        try {
            run(version, config);
        } catch (Exception e) {
            Main.getMessage().error(e);
        }
        config.set("version", CURRENT_VERSION);
        try {
            ((YamlConfiguration) config.getHandle()).save(configFile);
        } catch (IOException e) {
            Main.getMessage().error(e);
        }
        ((Main) Main.getInstance()).reloadConfigs();
    }

    private static void run(int version, YamlContext config) throws Exception {
        if (version == 0) {
            Main.getMessage().error("It is impossible to update files with such an old version!");
            version++;
            run(version, config);
        } else if (version == 1) {
            config.set("offer-min-price", 10);
            config.set("offer-max-price", 100000000);
            version++;
            run(version, config);
        } else if (version == 2) {
            new DBUpdate107().update();
            new Messages107().update();
            new TagUtil107().update();
            version++;
            run(version, config);
        } else if (version == 3) {
            new DBUpdate108().update();
            new MessagesUpdater().update();
            config.set("allow-buy-count", true);
            config.set("black-list", List.of("debug_stick"));
            version++;
            run(version, config);
        } else if (version == 4) {
            new DbCfg109().run();
            new DBUpdate109().update();
            new MySqlDBUpdater1().update();
            new MessagesUpdater().update();
            version++;
            run(version, config);
        }
    }
}
