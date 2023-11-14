package org.by1337.bauction.db.event;

import org.by1337.bauction.db.MemorySellItem;
import org.by1337.bauction.db.MemoryUnsoldItem;
import org.by1337.bauction.db.MemoryUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TakeUnsoldItemEvent implements Validatable {

    private boolean valid;
    private String reason = null;
    private final MemoryUser user;
    private final MemoryUnsoldItem sellItem;

    public TakeUnsoldItemEvent(@NotNull MemoryUser user, @NotNull MemoryUnsoldItem sellItem) {
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
    public MemoryUser getUser() {
        return user;
    }

    @NotNull
    public MemoryUnsoldItem getUnsoldItem() {
        return sellItem;
    }
}
