package org.by1337.bauction.datafix.config;

import org.bukkit.configuration.InvalidConfigurationException;
import org.by1337.bauction.util.ConfigUtil;

import java.io.IOException;

public class DbCfg109 {

    public void run() throws IOException, InvalidConfigurationException {
        var cfg = ConfigUtil.load("dbCfg.yml");
        cfg.set("server-id", "server-1");
        cfg.trySave();
    }
}
