package org.by1337.bauction.datafix;

import org.bukkit.plugin.Plugin;
import org.by1337.bauction.datafix.db.DBUpdate110;
import org.by1337.bauction.datafix.db.mysql.MySqlDBUpdater2;
import org.by1337.bauction.util.config.ConfigUtil;
import org.by1337.blib.configuration.YamlConfig;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.bauction.Main;
import org.by1337.bauction.datafix.config.*;
import org.by1337.bauction.datafix.db.mysql.MySqlDBUpdater1;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

public class UpdateManager {
    private final static int CURRENT_VERSION = 12;

    public static void checkUpdate() {
        if (Main.RUNNING_IN_IDE) return;

        YamlConfig config = ConfigUtil.load("config.yml");

        int version = config.getAsInteger("version", 0);

        if (CURRENT_VERSION != version) {
            Main.getMessage().warning("detected deprecated config!");
            Main.getMessage().log("start update...");
        } else {
            return;
        }

        try {
            run(version, config);
        } catch (Exception e) {
            Main.getMessage().error(e);
        }
        config.set("version", CURRENT_VERSION);
        try {
            config.save();
        } catch (IOException e) {
            Main.getMessage().error(e);
        }
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
            Main.getMessage().error("It is impossible to update files with such an old version!");
            makeDataFolderAsOld();
            new Messages107().update();
            new TagUtil107().update();
            version++;
            run(version, config);
        } else if (version == 3) {
            Main.getMessage().error("It is impossible to update files with such an old version!");
            makeDataFolderAsOld();
            new MessagesUpdater().update();
            config.set("allow-buy-count", true);
            config.set("black-list", List.of("debug_stick"));
            version++;
            run(version, config);
        } else if (version == 4) {
            new DbCfg109().run();
            Main.getMessage().error("It is impossible to update files with such an old version!");
            makeDataFolderAsOld();
            new MySqlDBUpdater1().update();
            new MessagesUpdater().update();
            version++;
            run(version, config);
        } else if (version == 5) {
            config.set("item-max-size", 70_000);
            config.set("compress-if-more-than", 30_000);
            config.set("maximum-uncompressed-item-size", 350000);
            new MessagesUpdater().update();
            new DBUpdate110().update();
            new MySqlDBUpdater2().update();
            version++;
            run(version, config);
        } else if (version == 6) {
            config.set("economy", "Vault");
            version++;
            run(version, config);
        } else if (version == 7) {
            new MySqlDBUpdater2().update();
            new MessagesUpdater().update();
            config.set("logging", false);
            config.set("default-slots", config.getAsInteger("max-slots", 10));
            config.set("max-slots", null);
            version++;
            run(version, config);
        } else if (version == 8) {
            new MySqlDBUpdater2().update();
            config.set("home-menu", "home");
            config.set("player-items-view-menu", "playerItemsView");
            try {
                File home = Main.getInstance().getDataFolder();
                File folder = new File(home, "_old");
                folder.mkdirs();
                for (String file : List.of("buyCount.yml", "confirm.yml", "itemsForSale.yml", "main.yml", "playerItemsView.yml", "unsoldItemList.yml")) {
                    File cfg = new File(home, file);
                    if (cfg.exists()) {
                        Files.move(cfg.toPath(), new File(folder, file).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (Throwable throwable) {
                Main.getMessage().error(throwable);
            }
        } else if (version == 9) {
            config.set("lang", "en_us");
            version++;
            run(version, config);
        } else if (version == 10) {
            config.set("BVault-setting.current-bank", "vault");
            version++;
            run(version, config);
        } else if (version == 11) {
            new MessagesUpdater().update();
            version++;
            run(version, config);
        }
    }
    private static void makeDataFolderAsOld(){
        File home = new File(Main.getInstance().getDataFolder() + "/data");
        if (home.exists()) {
            File moveTo = new File(Main.getInstance().getDataFolder() + "/data_old");
            if (moveTo.exists()) {
                moveTo = new File(Main.getInstance().getDataFolder() + "/data_old" + UUID.randomUUID());
            }
            moveTo.mkdirs();
            try {
                Files.move(home.toPath(), moveTo.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                Main.getMessage().error(e);
            }

        }
    }
}

