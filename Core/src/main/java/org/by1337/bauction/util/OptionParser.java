package org.by1337.bauction.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class OptionParser {
    private final Map<String, String> options;

    public OptionParser() {
        options = new HashMap<>();
    }

    public OptionParser(@NotNull String input) {
        this();
        parse(input);
    }

    public void parse(@NotNull String input) {
        if (input.isEmpty()) return;
        String[] arr = input.split(" ");
        String flag = null;
        for (String s : arr) {
            if (s.startsWith("-")) {
                flag = s.substring(1);
            } else {
                String val = options.get(Objects.requireNonNull(flag, "missing flag! Use -<flag> <value>"));
                options.put(flag, val == null ? s : val + " " + s);
            }
        }
    }

    public Map<String, String> getOptions() {
        return options;
    }
    public boolean has(String flag){
        return options.containsKey(flag);
    }

    @Nullable
    public String get(String flag) {
        return options.get(flag);
    }

    @Override
    public String toString() {
        return "OptionParser{" +
                "options=" + options +
                '}';
    }
}
