package org.by1337.bauction.storage.event;

import org.jetbrains.annotations.Nullable;

public interface Validatable {
    boolean isValid();
    void setValid(boolean flag);
    @Nullable
    String getReason();
    void setReason(String msg);
}
