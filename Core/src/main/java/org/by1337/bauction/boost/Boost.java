package org.by1337.bauction.boost;

import org.by1337.api.util.NameKey;

public class Boost {
    private final NameKey id;
    private final String permission;
    private final int externalSlots;
    private final long externalSellTime;

    public Boost(NameKey id, String permission, int externalSlots, long externalSellTime) {
        this.id = id;
        this.permission = permission;
        this.externalSlots = externalSlots;
        this.externalSellTime = externalSellTime;
    }

    public NameKey getId() {
        return id;
    }

    public String getPermission() {
        return permission;
    }

    public int getExternalSlots() {
        return externalSlots;
    }

    public long getExternalSellTime() {
        return externalSellTime;
    }
}
