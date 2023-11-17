package org.by1337.bauction.db;

import org.by1337.bauction.db.action.Action;

public interface DbActionListener {
    void update(Action action);
}
