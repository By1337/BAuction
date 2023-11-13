package org.by1337.bauction.db.json.kernel;

import org.by1337.api.BLib;
import org.by1337.bauction.db.MemoryUnsoldItem;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

class UnsoldItem {
    final String item;
    final long expired;
    final UUID owner;
    final UUID uuid;
    final long deleteVia;

    UnsoldItem(@NotNull String item, @NotNull UUID owner, long expired, long deleteVia) {
        this.item = item;
        this.expired = expired;
        this.owner = owner;
        this.deleteVia = deleteVia;
        uuid = UUID.randomUUID();
    }


    MemoryUnsoldItem toMemoryUnsoldItem() {
        return MemoryUnsoldItem.builder()
                .item(BLib.getApi().getItemStackSerialize().deserialize(item))
                .expired(expired)
                .owner(owner)
                .deleteVia(deleteVia)
                .uuid(uuid)
                .build();
    }

}
