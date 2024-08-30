package org.by1337.bauction.logg;

import com.google.errorprone.annotations.CanIgnoreReturnValue;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LoggUtil {
    public static File renameIfExist(File file, File logFolder) {
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
    public static String getDateFormat(Calendar calendar) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);

        return year + "-" +
                (month < 10 ? "0" + month : "" + month) + "-" +
                (day_of_month < 10 ? "0" + day_of_month : "" + day_of_month);

    }

    public static String getTimeFormat(Calendar calendar) {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);

        return (hour < 10 ? "0" + hour : "" + hour) + ":" +
                (minute < 10 ? "0" + minute : "" + minute) + ":" +
                (second < 10 ? "0" + second : "" + second);

    }

    public static Formatter getFormatter() {
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

    @CanIgnoreReturnValue
    public static StringBuilder writeStackTrace(Throwable throwable, StringBuilder sb) {
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
        return sb;
    }
}
