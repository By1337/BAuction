package org.by1337.bauction.db.event;

import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.auc.User;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.db.kernel.CUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SellItemEvent implements Validatable {
    private boolean valid;
    private String reason = null;
    private final User user;
    private final SellItem sellItem;


    public SellItemEvent(@NotNull User user, @NotNull SellItem sellItem) {
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
