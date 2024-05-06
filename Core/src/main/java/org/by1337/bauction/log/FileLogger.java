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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
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
    public void on(EventBuyItem event){
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
    public void on(EventBuyItemCount event){
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
    public void log(String log){
        logHandler.publish(new LogRecord(Level.INFO, log));
    }
    private void iniHandlers() throws IOException {
        File logFile = renameIfExist(new File(logFolder, "latest.log"));
        logHandler = new FileHandler(logFile.getPath());

        logHandler.setFormatter(getFormatter());
        logHandler.setLevel(Level.ALL);
        logHandler.setEncoding(StandardCharsets.UTF_8.name());
    }

    public void close() {
        logHandler.close();
        renameIfExist(new File(logFolder, "latest.log"));
    }

    private File renameIfExist(File file) {
        if (file.exists()) {
            String fileName = getDateFormat(Calendar.getInstance());
            File renamedLogFile = new File(logFolder, fileName + ".log");
            int x = 1;
            while (renamedLogFile.exists()) {
                renamedLogFile = new File(logFolder, fileName + "-" + x + ".log");
                x++;
            }
            file.renameTo(renamedLogFile);
        }
        return file;
    }

    private String getDateFormat(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);

        return year + "-" +
                (month < 10 ? "0" + month : "" + month) + "-" +
                (day_of_month < 10 ? "0" + day_of_month : "" + day_of_month);

    }

    private String getTimeFormat(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        return (hour < 10 ? "0" + hour : "" + hour) + ":" +
                (minute < 10 ? "0" + minute : "" + minute) + ":" +
                (second < 10 ? "0" + second : "" + second);

    }

    private Formatter getFormatter() {
        return new Formatter() {
            @Override
            public String format(LogRecord record) {
                StringBuilder sb = new StringBuilder("[");

                sb.append(getTimeFormat(Calendar.getInstance())).append(" ");

                sb.append(record.getLevel().getName()).append("] ");
                sb.append(formatMessage(record)).append("\n");
                if (record.getThrown() != null) {
                    writeStackTrace(record.getThrown(), sb);
                }
                return sb.toString();
            }
        };
    }

    public static void writeStackTrace(Throwable throwable, StringBuilder sb) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        sb.append(stringWriter);

        try {
            stringWriter.close();
            printWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
