package org.by1337.bauction.db.kernel;

import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.ItemHolder;
import org.by1337.bauction.common.db.type.UnsoldItem;
import org.by1337.bauction.lang.Lang;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.nbt.CompressedNBT;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.impl.ByteArrNBT;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

public class PluginUnsoldItem extends Placeholder implements ItemHolder {
    private UnsoldItem source;
    @Nullable
    private transient ItemStack itemStack;

    public PluginUnsoldItem(UnsoldItem source) {
        this.source = source;
        init();
    }

    void setSource(UnsoldItem source) {
        this.source = source;
        init();
    }

    public UnsoldItem getSource() {
        return source;
    }

    private void init() {
        registerPlaceholder("{expired}", () -> Main.getTimeUtil().getFormat(getExpired()));
        registerPlaceholder("{delete_via}", () -> Main.getTimeUtil().getFormat(getDeleteVia()));
        registerPlaceholder("{id}", this::getId);
        registerPlaceholder("{item_name}", () -> getItemStack().getItemMeta() != null && getItemStack().getItemMeta().hasDisplayName() ?
                getItemStack().getItemMeta().getDisplayName() :
                Lang.getMessage(getItemStack().getType().name().toLowerCase()));
    }

    public ItemStack getItemStack() {
        if (itemStack == null) {
            if (getItem() instanceof ByteArrNBT arrNBT) {
                CompoundTag tag = (CompoundTag) new CompressedNBT(arrNBT.getValue()).decompress();
                itemStack = BLib.getApi().getParseCompoundTag().create(tag);
            } else {
                itemStack = BLib.getApi().getParseCompoundTag().create((CompoundTag) getItem());
            }
        }
        return itemStack.clone();
    }

    public NBT getItem() {
        return source.getItem();
    }

    public long getExpired() {
        return source.getExpired();
    }

    public UUID getSellerUuid() {
        return source.getSellerUuid();
    }

    public long getId() {
        return source.getId();
    }

    public long getDeleteVia() {
        return source.getDeleteVia();
    }

    public CompoundTag getExtra() {
        return source.getExtra();
    }

    @Override
    public String toString() {
        return "PluginUnsoldItem{" +
               "source=" + source +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginUnsoldItem that = (PluginUnsoldItem) o;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return source.hashCode();
    }
}
