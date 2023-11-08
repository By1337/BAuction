package org.by1337.bauction.util;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NumberUtil {

    /**
     * Обрезает стоку
     * @param value 1272.3443284
     * @return 1272.34
     * При выводе числа игроку всегда это число должно проходить через этот метод
     * так как он не только обрезает строку, но и приводит её в нормальное состояние из 1.27232432234E9 в 1272324322.34
     */
    public static String format(double value){
        DecimalFormat df = new DecimalFormat("#.##");
        return df.format(value);
    }

    /**
     * Обрезает строку по аналогии с NumberUtil.format только возвращает обрезанный double
     * @param value 1272.3443284
     * @return 1272.34
     */
    public static double trim(double value){
        return Double.parseDouble(format(value));
    }

    public static long getTime(String s){
        StringBuilder number = new StringBuilder();
        StringBuilder type = new StringBuilder();
        long out = 0;
        char[] arr = s.toCharArray();

        for (char c : arr){
            if (Character.isDigit(c)){
                if (!type.isEmpty() && !number.isEmpty()){
                    out += getResult(Integer.parseInt(number.toString()), type.toString());
                    number = new StringBuilder();
                    type = new StringBuilder();
                }
                number.append(c);
            }else {
                type.append(c);
            }
        }
        if (!type.isEmpty() && !number.isEmpty()){
            out += getResult(Integer.parseInt(number.toString()), type.toString());
        }
        return out;
    }
    private static long getResult(int x, String s){
        return switch (s) {
            case "s" -> 1000L * x;
            case "m" -> 60000L * x;
            case "h" -> 3600000L * x;
            case "d" -> 86400000L * x;
            case "w" -> 604800000L * x;
            case "mo" -> 2629746000L * x;
            case "y" -> 31556908800L * x;
            default -> 0;
        };
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
