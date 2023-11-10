package org.by1337.bauction;

import org.by1337.api.chat.Placeholderable;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface User extends Placeholderable {
    @NotNull
    String getNickName();
    @NotNull
    UUID getUuid();
    int getExternalSlots();
    long getExternalSellTime();
    int getDealCount();
    double getDealSum();

}
