package org.by1337.bauction.util;

import org.by1337.bauction.Main;

public class TimeUtil {

    public static String getFormat(long time) {
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
            if (time < currentTimeMillis) {

                return formattedTime + " " + Main.getCfg().getMessage().getAsString("time-format.ago");
            } else {
                return Main.getCfg().getMessage().getAsString("time-format.in") + " " + formattedTime;
            }

        } else {
            return Main.getCfg().getMessage().getAsString("time-format.just-now");
        }
    }

    //%years% %months% %days% %hours% %minutes% %seconds%
    private static String formatTime(long years, long months, long days, long hours, long minutes, long seconds) {
        String str = getFormat(years, months, days, hours, minutes, seconds);
        if (years != 0) {
            if (years > 10) return Main.getCfg().getMessage().getAsString("never");
            str = str.replace("%years%", String.format("%s %s", years, getPluralForm(years, Main.getCfg().getMessage().getAsString("time-format.years.form-1", "?"), Main.getCfg().getMessage().getAsString("time-format.years.form-2", "?"), Main.getCfg().getMessage().getAsString("time-format.years.form-5", "?"))));
        }
        else
            str = str.replace("%years%", "");
        if (months != 0)
            str = str.replace("%months%", String.format("%s %s", months, getPluralForm(months, Main.getCfg().getMessage().getAsString("time-format.months.form-1", "?"), Main.getCfg().getMessage().getAsString("time-format.months.form-2", "?"), Main.getCfg().getMessage().getAsString("time-format.months.form-5", "?"))));
        else
            str = str.replace("%months%", "");
        if (days != 0)
            str = str.replace("%days%", String.format("%s %s", days, getPluralForm(days, Main.getCfg().getMessage().getAsString("time-format.days.form-1", "?"), Main.getCfg().getMessage().getAsString("time-format.days.form-2", "?"), Main.getCfg().getMessage().getAsString("time-format.days.form-5", "?"))));
        else
            str = str.replace("%days%", "");
        if (hours != 0)
            str = str.replace("%hours%", String.format("%s %s", hours, getPluralForm(hours, Main.getCfg().getMessage().getAsString("time-format.hours.form-1", "?"), Main.getCfg().getMessage().getAsString("time-format.hours.form-2", "?"), Main.getCfg().getMessage().getAsString("time-format.hours.form-5", "?"))));
        else
            str = str.replace("%hours%", "");
        if (minutes != 0)
            str = str.replace("%minutes%", String.format("%s %s", minutes, getPluralForm(minutes, Main.getCfg().getMessage().getAsString("time-format.minutes.form-1", "?"), Main.getCfg().getMessage().getAsString("time-format.minutes.form-2", "?"), Main.getCfg().getMessage().getAsString("time-format.minutes.form-5", "?"))));
        else
            str = str.replace("%minutes%", "");
        if (seconds != 0)
            str = str.replace("%seconds%", String.format("%s %s", seconds, getPluralForm(seconds, Main.getCfg().getMessage().getAsString("time-format.seconds.form-1", "?"), Main.getCfg().getMessage().getAsString("time-format.seconds.form-2", "?"), Main.getCfg().getMessage().getAsString("time-format.seconds.form-5", "?"))));
        else
            str = str.replace("%seconds%", "");
        return str;
    }

    private static String getPluralForm(long number, String form1, String form2, String form5) {
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

    private static String getFormat(long years, long months, long days, long hours, long minutes, long seconds) {
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
        return Main.getCfg().getMessage().getAsString("time-format.formats." + str, "error");
    }
}
