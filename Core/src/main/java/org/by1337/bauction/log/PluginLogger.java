package org.by1337.bauction.log;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.*;

public class PluginLogger {
    private final File logFolder;
    private final Logger logger;
    private FileHandler logHandler;

    public PluginLogger(File logFolder, Logger logger) {
        this.logFolder = logFolder;
        logFolder.mkdirs();
        this.logger = logger;
        try {
            iniHandlers();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        logger.addHandler(logHandler);
        logFolder.mkdirs();

    }

    public void log(String log) {
        logHandler.publish(new LogRecord(Level.INFO, log));
    }

    private void iniHandlers() throws IOException {
        File logFile = LogUtil.renameIfExist(new File(logFolder, "latest.log"), logFolder);
        logHandler = new FileHandler(logFile.getPath());

        logHandler.setFormatter(new CustomFormatter());
        logHandler.setLevel(Level.ALL);
        logHandler.setEncoding(StandardCharsets.UTF_8.name());
    }

    public void close() {
        logHandler.close();
        LogUtil.renameIfExist(new File(logFolder, "latest.log"), logFolder);
        logger.removeHandler(logHandler);
    }

    static class CustomFormatter extends SimpleFormatter {
        private static final String FORMAT = "[%s] [%s/%s]: %s%n";
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        @Override
        public synchronized String format(LogRecord record) {
            String date = dateFormat.format(new Date(record.getMillis()));
            String threadName = Thread.currentThread().getName();
            String level = record.getLevel().getLocalizedName();
            String message = formatMessage(record);
            return String.format(FORMAT, date, threadName, level, message);
        }
    }
}
