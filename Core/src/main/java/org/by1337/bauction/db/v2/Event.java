package org.by1337.bauction.db.v2;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.Nullable;

public abstract class Event {
    protected boolean valid;
    protected Component reason = null;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public Component getReason() {
        return reason;
    }

    public void setReason(@Nullable Component reason) {
        this.reason = reason;
    }
}
