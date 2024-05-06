package org.by1337.bauction.db.kernel;

import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.db.kernel.util.InsertBuilder;
import org.by1337.bauction.network.ByteBuffer;
import org.by1337.blib.BLib;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.UnsoldItem;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.serialize.SerializeUtils;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.api.util.UniqueName;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

public class CUnsoldItem extends Placeholder implements UnsoldItem {
    final String item;
    final long expired;
    final UUID sellerUuid;
    final UniqueName uniqueName;
    final long deleteVia;
    @Nullable
    private transient ItemStack itemStack;
    final boolean compressed;


    public String toSql(String table) {
        InsertBuilder insertBuilder = new InsertBuilder(table);
        insertBuilder.add("uuid", uniqueName.getKey());
        insertBuilder.add("seller_uuid", sellerUuid.toString());
        insertBuilder.add("item", item);
        insertBuilder.add("delete_via", deleteVia);
        insertBuilder.add("expired", expired);
        insertBuilder.add("compressed", compressed);
        return insertBuilder.build();
    }

    public static CUnsoldItem fromResultSet(ResultSet resultSet) throws SQLException {
        String item = resultSet.getString("item");
        long expired = resultSet.getLong("expired");
        String sellerUuidString = resultSet.getString("seller_uuid");
        UniqueName uniqueName = new CUniqueName(resultSet.getString("uuid"));

        long deleteVia = resultSet.getLong("delete_via");
        boolean compressed = resultSet.getBoolean("compressed");
        return new CUnsoldItem(item, expired, UUID.fromString(sellerUuidString), uniqueName, deleteVia, compressed);
    }

    public CUnsoldItem(String item, long expired, UUID sellerUuid, UniqueName uniqueName, long deleteVia, boolean compressed) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.uniqueName = uniqueName;
        this.deleteVia = deleteVia;
        this.compressed = compressed;
        init();
    }

    public CUnsoldItem(String item, long expired, UUID sellerUuid, UniqueName uniqueName, long deleteVia, @Nullable ItemStack itemStack, boolean compressed) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.uniqueName = uniqueName;
        this.deleteVia = deleteVia;
        this.itemStack = itemStack;
        this.compressed = compressed;
        init();
    }

    public CUnsoldItem(@NotNull String item, @NotNull UUID sellerUuid, long expired, long deleteVia, boolean compressed) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.deleteVia = deleteVia;
        this.compressed = compressed;
        uniqueName = Main.getUniqueNameGenerator().getNextCombination();
        init();
    }

    private void init() {
        registerPlaceholder("{expired}", () -> Main.getTimeUtil().getFormat(expired));
        registerPlaceholder("{delete_via}", () -> Main.getTimeUtil().getFormat(deleteVia));
        registerPlaceholder("{id}", () -> String.valueOf(uniqueName.getKey()));
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
                sellerUuid != null &&
                uniqueName != null;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeUTF(item);
            data.writeLong(expired);
            SerializeUtils.writeUUID(sellerUuid, data);
            data.writeUTF(uniqueName.getKey());
            data.writeLong(deleteVia);
            data.writeBoolean(compressed);
            data.flush();
            return out.toByteArray();
        }
    }

    public static CUnsoldItem fromBytes(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {

            String item = in.readUTF();
            long expired = in.readLong();
            UUID sellerUuid = SerializeUtils.readUUID(in);
            UniqueName uniqueName = new CUniqueName(
                    in.readUTF()
            );
            long deleteVia = in.readLong();
            boolean compressed = in.readBoolean();
            return new CUnsoldItem(
                    item, expired, sellerUuid, uniqueName, deleteVia, compressed
            );
        }
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

    public UniqueName getUniqueName() {
        return uniqueName;
    }

    public long getDeleteVia() {
        return deleteVia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CUnsoldItem that = (CUnsoldItem) o;
        return expired == that.expired && deleteVia == that.deleteVia && Objects.equals(item, that.item) && Objects.equals(sellerUuid, that.sellerUuid) && Objects.equals(uniqueName, that.uniqueName) && compressed == that.compressed;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(uniqueName.getKey().toCharArray());
    }


    @Override
    public String toString() {
        return "CUnsoldItem{" +
                "item='" + item + '\'' +
                ", expired=" + expired +
                ", sellerUuid=" + sellerUuid +
                ", uniqueName=" + uniqueName +
                ", deleteVia=" + deleteVia +
                '}';
    }
}
