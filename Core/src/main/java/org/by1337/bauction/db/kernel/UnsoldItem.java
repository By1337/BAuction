package org.by1337.bauction.db.kernel;

import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.ItemHolder;
import org.by1337.bauction.db.io.codec.Codec;
import org.by1337.bauction.db.io.codec.UnsoldItemCodec;
import org.by1337.bauction.lang.Lang;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.nbt.CompressedNBT;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.impl.ByteArrNBT;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class UnsoldItem extends Placeholder implements ItemHolder {
    public static final Codec<UnsoldItem> CODEC = new UnsoldItemCodec();
    public final NBT item;
    public final long expired;
    public final UUID sellerUuid;
    public final long id;
    public final long deleteVia;
    @Nullable
    private transient ItemStack itemStack;
    public final CompoundTag extra;

    public UnsoldItem(NBT item, long expired, UUID sellerUuid, long id, long deleteVia, CompoundTag extra) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.id = id;
        this.deleteVia = deleteVia;
        this.extra = extra;
        init();
    }

    public UnsoldItem(NBT item, long expired, UUID sellerUuid, long id, long deleteVia, @Nullable ItemStack itemStack, CompoundTag extra) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.id = id;
        this.deleteVia = deleteVia;
        this.itemStack = itemStack;
        this.extra = extra;
        init();
    }

    public UnsoldItem(@NotNull NBT item, @NotNull UUID sellerUuid, long expired, long deleteVia, CompoundTag extra) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.deleteVia = deleteVia;
        this.extra = extra;
        id = Main.getUniqueIdGenerator().nextId();
        init();
    }

    private void init() {
        registerPlaceholder("{expired}", () -> Main.getTimeUtil().getFormat(expired));
        registerPlaceholder("{delete_via}", () -> Main.getTimeUtil().getFormat(deleteVia));
        registerPlaceholder("{id}", () -> id);
        registerPlaceholder("{item_name}", () -> getItemStack().getItemMeta() != null && getItemStack().getItemMeta().hasDisplayName() ?
                getItemStack().getItemMeta().getDisplayName() :
                Lang.getMessage(getItemStack().getType().name().toLowerCase()));
    }

    public ItemStack getItemStack() {
        if (itemStack == null) {
            if (item instanceof ByteArrNBT arrNBT) {
                CompoundTag tag = (CompoundTag) new CompressedNBT(arrNBT.getValue()).decompress();
                itemStack = BLib.getApi().getParseCompoundTag().create(tag);
            } else {
                itemStack = BLib.getApi().getParseCompoundTag().create((CompoundTag) item);
            }
        }
        return itemStack.clone();
    }

    public boolean isValid() {
        return item != null &&
               sellerUuid != null;
    }


    public NBT getItem() {
        return item;
    }

    public long getExpired() {
        return expired;
    }

    public UUID getSellerUuid() {
        return sellerUuid;
    }

    public long getId() {
        return id;
    }

    public long getDeleteVia() {
        return deleteVia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnsoldItem that = (UnsoldItem) o;
        return expired == that.expired && id == that.id && deleteVia == that.deleteVia && Objects.equals(item, that.item) && Objects.equals(sellerUuid, that.sellerUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, expired, sellerUuid, id, deleteVia);
    }

    @Override
    public String toString() {
        return "UnsoldItem{" +
               "item='" + item + '\'' +
               ", expired=" + expired +
               ", sellerUuid=" + sellerUuid +
               ", id=" + id +
               ", deleteVia=" + deleteVia +
               '}';
    }
}
