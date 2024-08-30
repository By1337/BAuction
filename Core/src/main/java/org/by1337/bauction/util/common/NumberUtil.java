package org.by1337.bauction.util.common;

import org.by1337.bauction.util.time.TimeParser;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberUtil {

    /**
     * Crops the stock
     *
     * @param value 1272.3443284
     * @return 1272.34
     * When outputting a number to the player, the number should always be passed through this method
     * as it not only trims the string, but also normalises it from 1.2723243223434E9 to 1272324322.34.
     */
    public static String format(double value) {
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value);
    }

    public static String formatNumberWithThousandsSeparator(double raw) {
        return formatNumberWithThousandsSeparator(format(raw));
    }

    public static String formatNumberWithThousandsSeparator(String raw) {
        StringBuilder formatted = new StringBuilder();
        String[] parts = raw.split("\\.");
        String integerPart = parts[0];
        String decimalPart = (parts.length > 1) ? "." + parts[1] : "";

        char[] integerDigits = integerPart.toCharArray();
        for (int i = integerDigits.length - 1, count = 0; i >= 0; i--, count++) {
            if (count > 0 && count % 3 == 0) {
                formatted.append(" ");
            }
            formatted.append(integerDigits[i]);
        }
        return formatted.reverse() + decimalPart;
    }

    /**
     * Truncates string similar to NumberUtil.format but returns truncated double
     *
     * @param value 1272.3443284
     * @return 1272.34
     */
    public static double trim(double value) {
        return Double.parseDouble(format(value));
    }

    /**
     * @param s time, example '2d' or '1y2w23d
     * @return time in millis
     * @deprecated use {@link TimeParser#parse(String)}
     */
    @Deprecated(forRemoval = true)
    public static long getTime(String s) {
        return TimeParser.parse(s);
    }


    public static boolean isDouble(String num) {
        Pattern pattern = Pattern.compile("(-?\\d+)([.])?(\\d+)?");
        Matcher matcher = pattern.matcher(num);
        if (matcher.find()) {
            return matcher.group().equals(num);
        } else
            return false;
    }
}
