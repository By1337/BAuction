package org.by1337.bauction.util;

import java.util.regex.Pattern;

public class UUIDUtils {
    private static final Pattern UUID_REGEX =
            Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

    public static boolean isUUID(String str){
        return UUID_REGEX.matcher(str).matches();
    }
}
