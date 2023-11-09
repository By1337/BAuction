package org.by1337.bauction;

import org.bukkit.inventory.ItemStack;
import org.by1337.api.BLib;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.util.TimeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UnsoldItemImpl implements Placeholderable {
    private final String item;
    private final long expired;
    private final UUID owner;
    private final long deleteVia;
    private transient ItemStack itemStack;

    public UnsoldItemImpl(@NotNull String item, @NotNull UUID owner, long expired, long deleteVia) {
        this.item = item;
        this.expired = expired;
        this.owner = owner;
        this.deleteVia = deleteVia;
    }

    @NotNull
    public String getItem() {
        return item;
    }

    public long getExpired() {
        return expired;
    }

    public long getDeleteVia() {
        return deleteVia;
    }

    @NotNull
    public UUID getOwner() {
        return owner;
    }

    @NotNull
    public ItemStack getItemStack() {
        if (itemStack == null) {
            itemStack = BLib.getApi().getItemStackSerialize().deserialize(item);
        }
        return itemStack.clone();
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (true) {
            if (sb.indexOf("{expired}") != -1) {
                sb.replace(sb.indexOf("{expired}"), sb.indexOf("{expired}") + "{expired}".length(), TimeUtil.getFormat(expired));
                continue;
            }
            if (sb.indexOf("{delete-via}") != -1) {
                sb.replace(sb.indexOf("{delete-via}"), sb.indexOf("{delete-via}") + "{delete-via}".length(), TimeUtil.getFormat(deleteVia));
                continue;
            }
            break;
        }
        return sb.toString();
    }
}
