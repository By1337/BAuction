package org.by1337.bauction.db.kernel;

import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.ItemHolder;
import org.by1337.bauction.lang.Lang;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class UnsoldItem extends Placeholder implements ItemHolder {
    final String item;
    final long expired;
    final UUID sellerUuid;
    final long id;
    final long deleteVia;
    @Nullable
    private transient ItemStack itemStack;
    final boolean compressed;


    @Deprecated(forRemoval = true)
    public String toSql(String table) {
        return null;
    }

    @Deprecated(forRemoval = true)
    public static UnsoldItem fromResultSet(ResultSet resultSet) throws SQLException {
        return null;
    }

    public UnsoldItem(String item, long expired, UUID sellerUuid, long id, long deleteVia, boolean compressed) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.id = id;
        this.deleteVia = deleteVia;
        this.compressed = compressed;
        init();
    }

    public UnsoldItem(String item, long expired, UUID sellerUuid, long id, long deleteVia, @Nullable ItemStack itemStack, boolean compressed) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.id = id;
        this.deleteVia = deleteVia;
        this.itemStack = itemStack;
        this.compressed = compressed;
        init();
    }

    public UnsoldItem(@NotNull String item, @NotNull UUID sellerUuid, long expired, long deleteVia, boolean compressed) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.deleteVia = deleteVia;
        this.compressed = compressed;
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
            if (compressed) {
                itemStack = BLib.getApi().getItemStackSerialize().decompressAndDeserialize(item);
            } else {
                itemStack = BLib.getApi().getItemStackSerialize().deserialize(item);
            }
        }
        return itemStack.clone();
    }

    public boolean isValid() {
        return item != null &&
               sellerUuid != null;
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
        return expired == that.expired && id == that.id && deleteVia == that.deleteVia && compressed == that.compressed && Objects.equals(item, that.item) && Objects.equals(sellerUuid, that.sellerUuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, expired, sellerUuid, id, deleteVia, compressed);
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
