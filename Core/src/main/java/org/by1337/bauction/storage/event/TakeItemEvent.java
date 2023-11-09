package org.by1337.bauction.storage.event;

import org.by1337.bauction.SellItem;
import org.by1337.bauction.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TakeItemEvent implements Validatable {

    private boolean valid;
    private String reason = null;
    private final User user;
    private final SellItem sellItem;


    public TakeItemEvent(@NotNull User user, @NotNull SellItem sellItem) {
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
    public SellItem getSellItem() {
        return sellItem;
    }
}
