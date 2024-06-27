package org.by1337.bauction.util.time;

import org.by1337.blib.configuration.YamlContext;
import org.by1337.bauction.Main;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeUtil {

    private Map<String, String> map;

    public TimeUtil() {
        load();
    }

    public void reload() {
        load();
    }

    public void load() {
        map = new HashMap<>();
        YamlContext context = Main.getCfg().getMessage();

        map.put("time-format.ago", context.getAsString("time-format.ago"));
        map.put("time-format.in", context.getAsString("time-format.in"));
        map.put("time-format.just-now", context.getAsString("time-format.just-now"));
        map.put("time-format.never", context.getAsString("time-format.never"));

        for (String format : List.of("formats", "years", "months", "days", "hours", "minutes", "seconds")) {
            for (Map.Entry<String, String> entry : context.getMap("time-format." + format, String.class).entrySet()) {
                map.put("time-format." + format + "." + entry.getKey(), entry.getValue());
            }
        }
    }

    public String getFormat(long time) {
        return getFormat(time, true);
    }

    public String getFormat(long time, boolean prefix) {
        long currentTimeMillis = System.currentTimeMillis();
        long timeDifferenceMillis = currentTimeMillis - time;
        timeDifferenceMillis = timeDifferenceMillis < 0 ? -timeDifferenceMillis : timeDifferenceMillis;

        long seconds = timeDifferenceMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        long months = days / 30;
        long years = months / 12;

        if (timeDifferenceMillis > 999) {
            minutes %= 60;
            seconds %= 60;
            hours %= 24;
            days %= 30;
            months %= 12;
            String formattedTime = formatTime(years, months, days, hours, minutes, seconds);
            if (!prefix) return formattedTime;
            if (time < currentTimeMillis) {
                return formattedTime + " " + map.get("time-format.ago");
            } else {
                return map.get("time-format.in") + " " + formattedTime;
            }

        } else {
            return map.get("time-format.just-now");
        }
    }

    //%years% %months% %days% %hours% %minutes% %seconds%
    private String formatTime(long years, long months, long days, long hours, long minutes, long seconds) {
        String str = getFormat(years, months, days, hours, minutes, seconds);
        if (years != 0) {
            //if (years > 10) return ;
            str = str.replace("%years%", String.format("%s %s", years, getPluralForm(years, map.getOrDefault("time-format.years.form-1", "?"), map.getOrDefault("time-format.years.form-2", "?"), map.getOrDefault("time-format.years.form-5", "?"))));
        } else
            str = str.replace("%years%", "");
        if (months != 0)
            str = str.replace("%months%", String.format("%s %s", months, getPluralForm(months, map.getOrDefault("time-format.months.form-1", "?"), map.getOrDefault("time-format.months.form-2", "?"), map.getOrDefault("time-format.months.form-5", "?"))));
        else
            str = str.replace("%months%", "");
        if (days != 0)
            str = str.replace("%days%", String.format("%s %s", days, getPluralForm(days, map.getOrDefault("time-format.days.form-1", "?"), map.getOrDefault("time-format.days.form-2", "?"), map.getOrDefault("time-format.days.form-5", "?"))));
        else
            str = str.replace("%days%", "");
        if (hours != 0)
            str = str.replace("%hours%", String.format("%s %s", hours, getPluralForm(hours, map.getOrDefault("time-format.hours.form-1", "?"), map.getOrDefault("time-format.hours.form-2", "?"), map.getOrDefault("time-format.hours.form-5", "?"))));
        else
            str = str.replace("%hours%", "");
        if (minutes != 0)
            str = str.replace("%minutes%", String.format("%s %s", minutes, getPluralForm(minutes, map.getOrDefault("time-format.minutes.form-1", "?"), map.getOrDefault("time-format.minutes.form-2", "?"), map.getOrDefault("time-format.minutes.form-5", "?"))));
        else
            str = str.replace("%minutes%", "");
        if (seconds != 0)
            str = str.replace("%seconds%", String.format("%s %s", seconds, getPluralForm(seconds, map.getOrDefault("time-format.seconds.form-1", "?"), map.getOrDefault("time-format.seconds.form-2", "?"), map.getOrDefault("time-format.seconds.form-5", "?"))));
        else
            str = str.replace("%seconds%", "");
        return str;
    }

    private String getPluralForm(long number, String form1, String form2, String form5) {
        number = Math.abs(number);
        long lastDigit = number % 10;
        long lastTwoDigits = number % 100;
        if (lastTwoDigits >= 11 && lastTwoDigits <= 19) {
            return form5;
        } else if (lastDigit == 1) {
            return form1;
        } else if (lastDigit >= 2 && lastDigit <= 4) {
            return form2;
        } else {
            return form5;
        }
    }

    private String getFormat(long years, long months, long days, long hours, long minutes, long seconds) {
        int height = (years != 0 ? 8 : 0) ^ (months != 0 ? 16 : 0) ^ (days != 0 ? 32 : 0) ^ (hours != 0 ? 64 : 0) ^ (minutes != 0 ? 128 : 0) ^ (seconds != 0 ? 256 : 0);
        String str;
        if ((height & 8) != 0) {
            str = "years";
        } else if ((height & 16) != 0) {
            str = "months";
        } else if ((height & 32) != 0) {
            str = "days";
        } else if ((height & 64) != 0) {
            str = "hours";
        } else if ((height & 128) != 0) {
            str = "minutes";
        } else if ((height & 256) != 0) {
            str = "seconds";
        } else {
            Main.getMessage().error("height=" + height);
            str = "error";
        }
        return map.getOrDefault("time-format.formats." + str, "error");
    }
}
