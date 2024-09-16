package org.by1337.bauction.common.db.event;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class Event {
    protected boolean valid = true;
    @Nullable
    protected String reason = null;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @NotNull
    public String getReason() {
        return Objects.requireNonNullElse(reason, "no reason");
    }

    public void setReason(@Nullable String reason) {
        this.reason = reason;
    }
}
