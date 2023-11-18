package org.by1337.bauction.db.kernel;

import lombok.Builder;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.BLib;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
@Builder
public class UnsoldItem implements Placeholderable {
    final String item;
    final long expired;
    final UUID owner;
    final UUID uuid;
    final long deleteVia;
    private transient ItemStack itemStack;

    public UnsoldItem(String item, long expired, UUID owner, UUID uuid, long deleteVia, ItemStack itemStack) {
        this.item = item;
        this.expired = expired;
        this.owner = owner;
        this.uuid = uuid;
        this.deleteVia = deleteVia;
        this.itemStack = itemStack;
    }

    public  UnsoldItem(@NotNull String item, @NotNull UUID owner, long expired, long deleteVia) {
        this.item = item;
        this.expired = expired;
        this.owner = owner;
        this.deleteVia = deleteVia;
        uuid = UUID.randomUUID();
    }


//    UnsoldItem toMemoryUnsoldItem() {
//        return UnsoldItem.builder()
//                .item(BLib.getApi().getItemStackSerialize().deserialize(item))
//                .expired(expired)
//                .owner(owner)
//                .deleteVia(deleteVia)
//                .uuid(uuid)
//                .build();
//    }

    public ItemStack getItemStack() {
        if (itemStack == null){
            itemStack = BLib.getApi().getItemStackSerialize().deserialize(item);
        }
        return itemStack;
    }

    public String getItem() {
        return item;
    }

    public long getExpired() {
        return expired;
    }

    public UUID getOwner() {
        return owner;
    }

    public UUID getUuid() {
        return uuid;
    }

    public long getDeleteVia() {
        return deleteVia;
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
}
