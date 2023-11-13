package org.by1337.bauction.db.event;

import org.by1337.bauction.db.json.SellItem;
import org.by1337.bauction.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuyItemCountEvent implements Validatable {

    private boolean valid;
    private String reason = null;
    private final User user;
    private final SellItem sellItem;
    private final int count;

    public BuyItemCountEvent(@NotNull User user, @NotNull SellItem sellItem, int count) {
        this.user = user;
        this.sellItem = sellItem;
        this.count = count;
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

    public int getCount() {
        return count;
    }

    @NotNull
    public SellItem getSellItem() {
        return sellItem;
    }
}
