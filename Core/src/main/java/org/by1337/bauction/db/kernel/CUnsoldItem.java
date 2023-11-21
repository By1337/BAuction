package org.by1337.bauction.db.kernel;

import lombok.Builder;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.BLib;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.UnsoldItem;
import org.by1337.bauction.lang.Lang;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
@Builder
public class CUnsoldItem implements UnsoldItem {
    final String item;
    final long expired;
    final UUID sellerUuid;
    final UUID uuid;
    final long deleteVia;
    private transient ItemStack itemStack;

    public CUnsoldItem(String item, long expired, UUID sellerUuid, UUID uuid, long deleteVia) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.uuid = uuid;
        this.deleteVia = deleteVia;
    }

    public CUnsoldItem(String item, long expired, UUID sellerUuid, UUID uuid, long deleteVia, ItemStack itemStack) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.uuid = uuid;
        this.deleteVia = deleteVia;
        this.itemStack = itemStack;
    }

    public CUnsoldItem(@NotNull String item, @NotNull UUID sellerUuid, long expired, long deleteVia) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.deleteVia = deleteVia;
        uuid = UUID.randomUUID();
    }

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

    public UUID getSellerUuid() {
        return sellerUuid;
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
            if (sb.indexOf("{item_name}") != -1) {
                sb.replace(sb.indexOf("{item_name}"), sb.indexOf("{item_name}") + "{item_name}".length(),
                        getItemStack().getItemMeta() != null && getItemStack().getItemMeta().hasDisplayName() ?
                                getItemStack().getItemMeta().getDisplayName() :
                                Lang.getMessages(getItemStack().getType().name().toLowerCase())
                );
                continue;
            }
            break;
        }
        return sb.toString();
    }
}
