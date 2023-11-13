package org.by1337.bauction.db.event;

import org.jetbrains.annotations.Nullable;

public interface Validatable {
    boolean isValid();
    void setValid(boolean flag);
    @Nullable
    String getReason();
    void setReason(String msg);
}
