package org.by1337.bauction.log;

import com.google.common.base.Joiner;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.by1337.bauction.api.event.EventBuyItem;
import org.by1337.bauction.api.event.EventBuyItemCount;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FileLogger implements Listener {
    private final File logFolder;
    private FileHandler logHandler;

    public FileLogger(File logFolder, Plugin plugin) {
        this.logFolder = logFolder;
        logFolder.mkdirs();
        try {
            iniHandlers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void on(EventBuyItem event) {
        log(String.format(
                "Player %s[%s] bought %s[%s] from player %s[%s] for %s coins.",
                event.getBuyer().getNickName(),
                event.getBuyer().getUuid(),
                event.getSellItem().getMaterial(),
                Joiner.on(", ").join(event.getSellItem().getTags()),
                event.getSellItem().getSellerName(),
                event.getSellItem().getSellerUuid(),
                event.getSellItem().getPrice()
        ));
    }

    @EventHandler
    public void on(EventBuyItemCount event) {
        log(String.format(
                "Player %s[%s] bought %s[%s] from player %s[%s] for %s coins in the amount of %s",
                event.getBuyer().getNickName(),
                event.getBuyer().getUuid(),
                event.getSellItem().getMaterial(),
                Joiner.on(", ").join(event.getSellItem().getTags()),
                event.getSellItem().getSellerName(),
                event.getSellItem().getSellerUuid(),
                event.getSellItem().getPriceForOne() * event.getCount(),
                event.getCount()
        ));
    }

    public void log(String log) {
        logHandler.publish(new LogRecord(Level.INFO, log));
    }

    private void iniHandlers() throws IOException {
        File logFile = LogUtil.renameIfExist(new File(logFolder, "latest.log"), logFolder);
        logHandler = new FileHandler(logFile.getPath());

        logHandler.setFormatter(LogUtil.getFormatter());
        logHandler.setLevel(Level.ALL);
        logHandler.setEncoding(StandardCharsets.UTF_8.name());
    }

    public void close() {
        logHandler.close();
        LogUtil.renameIfExist(new File(logFolder, "latest.log"), logFolder);
    }

}
