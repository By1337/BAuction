package org.by1337.bauction.db;

import org.bukkit.inventory.ItemStack;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.TimeUtil;

import java.util.UUID;

public class MemoryUnsoldItem implements Placeholderable {
    private final ItemStack item;
    private final long expired;
    private final UUID owner;
    private final UUID uuid;
    private final long deleteVia;

    public MemoryUnsoldItem(ItemStack item, long expired, UUID owner, UUID uuid, long deleteVia) {
        this.item = item;
        this.expired = expired;
        this.owner = owner;
        this.uuid = uuid;
        this.deleteVia = deleteVia;
    }

    public static MemoryUnsoldItemBuilder builder() {
        return new MemoryUnsoldItemBuilder();
    }


    public ItemStack getItem() {
        return item;
    }

    public long getExpired() {
        return expired;
    }

    public UUID getOwner() {
        return owner;
    }

    public long getDeleteVia() {
        return deleteVia;
    }

    public UUID getUuid() {
        return uuid;
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (true) {
            if (sb.indexOf("{expired}") != -1) {
                sb.replace(sb.indexOf("{expired}"), sb.indexOf("{expired}") + "{expired}".length(), Main.getTimeUtil().getFormat(expired));
                continue;
            }
            if (sb.indexOf("{delete_via}") != -1) {
                sb.replace(sb.indexOf("{delete_via}"), sb.indexOf("{delete_via}") + "{delete_via}".length(), Main.getTimeUtil().getFormat(deleteVia));
                continue;
            }
            if (sb.indexOf("{id}") != -1) {
                sb.replace(sb.indexOf("{id}"), sb.indexOf("{id}") + "{id}".length(), String.valueOf(uuid));
                continue;
            }
            break;
        }
        return sb.toString();
    }


    public static class MemoryUnsoldItemBuilder {
        private ItemStack item;
        private long expired;
        private UUID owner;
        private UUID uuid;
        private long deleteVia;

        MemoryUnsoldItemBuilder() {
        }

        public MemoryUnsoldItemBuilder item(ItemStack item) {
            this.item = item;
            return this;
        }

        public MemoryUnsoldItemBuilder expired(long expired) {
            this.expired = expired;
            return this;
        }

        public MemoryUnsoldItemBuilder owner(UUID owner) {
            this.owner = owner;
            return this;
        }

        public MemoryUnsoldItemBuilder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public MemoryUnsoldItemBuilder deleteVia(long deleteVia) {
            this.deleteVia = deleteVia;
            return this;
        }

        public MemoryUnsoldItem build() {
            return new MemoryUnsoldItem(this.item, this.expired, this.owner, this.uuid, this.deleteVia);
        }

    }
}
