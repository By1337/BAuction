package org.by1337.bauction.db.v2;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public abstract class Event {
    protected boolean valid = true;
    @Nullable
    protected Component reason = null;

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @NotNull
    public Component getReason() {
        return Objects.requireNonNullElse(reason, Component.text("no reason").color(TextColor.color(255, 0, 0)));
    }

    public void setReason(@Nullable Component reason) {
        this.reason = reason;
    }
}
