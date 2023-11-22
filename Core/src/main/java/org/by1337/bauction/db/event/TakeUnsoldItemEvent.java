package org.by1337.bauction.db.event;

import org.by1337.bauction.db.kernel.CUnsoldItem;
import org.by1337.bauction.db.kernel.CUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TakeUnsoldItemEvent implements Validatable {

    private boolean valid;
    private String reason = null;
    private final CUser user;
    private final CUnsoldItem sellItem;

    public TakeUnsoldItemEvent(@NotNull CUser user, @NotNull CUnsoldItem sellItem) {
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
    public CUser getUser() {
        return user;
    }

    @NotNull
    public CUnsoldItem getUnsoldItem() {
        return sellItem;
    }
}
