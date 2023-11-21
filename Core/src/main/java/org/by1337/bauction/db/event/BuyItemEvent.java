package org.by1337.bauction.db.event;


import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.db.kernel.СUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuyItemEvent implements Validatable {

    private boolean valid;
    private String reason = null;
    private final СUser user;
    private final CSellItem sellItem;

    public BuyItemEvent(@NotNull СUser user, @NotNull CSellItem sellItem) {
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
    public СUser getUser() {
        return user;
    }

    @NotNull
    public CSellItem getSellItem() {
        return sellItem;
    }
}
