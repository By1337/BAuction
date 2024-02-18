package org.by1337.bauction.db.kernel.util;

import java.util.HashMap;
import java.util.Map;

public class InsertBuilder {
    private final Map<String, Object> values = new HashMap<>();
    private final String table;

    public InsertBuilder(String table) {
        this.table = table;
    }

    public void add(String colum, Object value) {
        values.put(colum, value);
    }

    public String build() {
        if (values.isEmpty()) {
            throw new IllegalStateException("values is empty!");
        }
        StringBuilder sql = new StringBuilder("INSERT INTO ");
        sql.append(table).append(" (");
        StringBuilder values = new StringBuilder(" VALUES(");

        this.values.forEach((k, v) -> {

            if (k.contains("'") || k.contains("\\") || k.contains("\"")) {
                sql.append(quoteAndEscape(k));
            } else {
                sql.append(k);
            }
            sql.append(",");
            if (v instanceof String s) {
                values.append(quoteAndEscape(s));
            } else {
                values.append(v);
            }
            values.append(",");
        });
        sql.setLength(sql.length() - 1);
        values.setLength(values.length() - 1);

        sql.append(")").append(values).append(")");
        return sql.toString();
    }

    public String quoteAndEscape(String raw) {
        StringBuilder sb = new StringBuilder(" ");
        int x = 0;
        for (int i = 0; i < raw.length(); ++i) {
            char c = raw.charAt(i);
            if (c == '\\') {
                sb.append('\\');
            } else if (c == '"' || c == '\'') {
                if (x == 0) {
                    x = c == '"' ? '\'' : '"';
                }
                if (x == c) {
                    sb.append('\\');
                }
            }
            sb.append(c);
        }
        if (x == 0) {
            x = '\'';
        }
        sb.setCharAt(0, (char) x);
        sb.append((char) x);
        return sb.toString();
    }

}
