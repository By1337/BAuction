package org.by1337.bauction.util;

import org.by1337.blib.chat.Placeholderable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public abstract class Placeholder implements Placeholderable {
    protected final Map<String, Supplier<Object>> placeholders = new HashMap<>();

    public void registerPlaceholder(String placeholder, Supplier<Object> supplier) {
        placeholders.put(placeholder, supplier);
    }

    public void registerPlaceholders(Collection<Map.Entry<String, Supplier<Object>>> list) {
        for (var entry : list) {
            registerPlaceholder(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public String replace(String string) {
        StringBuilder sb = new StringBuilder(string);
        for (Map.Entry<String, Supplier<Object>> entry : placeholders.entrySet()) {
            String placeholder = entry.getKey();
            int len = placeholder.length();
            int pos = sb.indexOf(placeholder);
            while (pos != -1) {
                var replaceTo = String.valueOf(entry.getValue().get());
                sb.replace(pos, pos + len, replaceTo);
                pos = sb.indexOf(placeholder, pos + replaceTo.length());
            }
        }
        return sb.toString();
    }

    public Set<Map.Entry<String, Supplier<Object>>> entrySet() {
        return placeholders.entrySet();
    }

}
