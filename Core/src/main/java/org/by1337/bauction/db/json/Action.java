package org.by1337.bauction.db.json;

import org.by1337.bauction.db.json.kernel.ActionType;

public class Action<T> {
    private final long time = System.currentTimeMillis();
    private final ActionType type;
    private final T body;

    public Action(ActionType type, T body) {
        this.type = type;
        this.body = body;
    }

    public ActionType getType() {
        return type;
    }

    public T getBody() {
        return body;
    }

    public long getTime() {
        return time;
    }
}
