package org.by1337.bauction.db.event;

import org.by1337.bauction.db.kernel.UnsoldItem;
import org.by1337.bauction.db.kernel.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TakeUnsoldItemEvent implements Validatable {

    private boolean valid;
    private String reason = null;
    private final User user;
    private final UnsoldItem sellItem;

    public TakeUnsoldItemEvent(@NotNull User user, @NotNull UnsoldItem sellItem) {
        this.user = user;
        this.sellItem = sellItem;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void setValid(boolean flag) {
        valid = flag;
    }

    @Override
    public @Nullable String getReason() {
        return reason;
    }

    @Override
    public void setReason(String msg) {
        reason = msg;
    }

    @NotNull
    public User getUser() {
        return user;
    }

    @NotNull
    public UnsoldItem getUnsoldItem() {
        return sellItem;
    }
}
