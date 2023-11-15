package org.by1337.bauction.db.json;

import org.by1337.bauction.db.json.kernel.ActionType;

public class Action<T> {
  //  private final long time = System.currentTimeMillis();
    private final ActionType<T> type;
    private final T body;

    public Action(ActionType<T> type, T body) {
        this.type = type;
        this.body = body;
    }

    public ActionType<T> getType() {
        return type;
    }

    public T getBody() {
        return body;
    }


    public String toLog(){
        if (!type.isLogged()) throw new IllegalStateException(this.toString());
        return "{" +
                "type=" + type.getName() +
                ",body=" + body +
                '}';
    }

    @Override
    public String toString() {
        return "Action{" +
                "type=" + type +
                ", body=" + body +
                '}';
    }
}
