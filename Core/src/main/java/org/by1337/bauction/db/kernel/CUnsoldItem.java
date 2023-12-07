package org.by1337.bauction.db.kernel;

import lombok.Builder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.BLib;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.UnsoldItem;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.serialize.SerializeUtils;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.util.UniqueName;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
public class CUnsoldItem implements UnsoldItem {
    final String item;
    final long expired;
    final UUID sellerUuid;
    final UniqueName uniqueName;
    final long deleteVia;
    private transient ItemStack itemStack;

    public String toSql(String table) {
        return String.format(
                "INSERT INTO %s (uuid, seller_uuid, item, delete_via, expired)" +
                        "VALUES('%s', '%s', '%s', %s, %s)", table, uniqueName.getKey(), sellerUuid, item, deleteVia, expired
        );
    }

    public static CUnsoldItem fromResultSet(ResultSet resultSet) throws SQLException {
        String item = resultSet.getString("item");
        long expired = resultSet.getLong("expired");
        String sellerUuidString = resultSet.getString("seller_uuid");
        UniqueName uniqueName = new CUniqueName(resultSet.getString("uuid"));

        long deleteVia = resultSet.getLong("delete_via");

        return new CUnsoldItem(item, expired, UUID.fromString(sellerUuidString), uniqueName, deleteVia);
    }

    public CUnsoldItem(String item, long expired, UUID sellerUuid, UniqueName uniqueName, long deleteVia) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.uniqueName = uniqueName;
        this.deleteVia = deleteVia;
    }

    public CUnsoldItem(String item, long expired, UUID sellerUuid, UniqueName uniqueName, long deleteVia, ItemStack itemStack) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.uniqueName = uniqueName;
        this.deleteVia = deleteVia;
        this.itemStack = itemStack;
    }

    public CUnsoldItem(@NotNull String item, @NotNull UUID sellerUuid, long expired, long deleteVia) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.deleteVia = deleteVia;
        uniqueName = Main.getUniqueNameGenerator().getNextCombination();
    }

    public ItemStack getItemStack() {
        if (itemStack == null) {
            itemStack = BLib.getApi().getItemStackSerialize().deserialize(item);
        }
        return itemStack;
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
            data.writeUTF(sellerUuid.toString());
            data.writeUTF(uniqueName.getKey());
            data.writeInt(uniqueName.getSeed());
            data.writeLong(uniqueName.getPos());
            data.writeLong(deleteVia);
            data.flush();
            return out.toByteArray();
        }
    }

    public static CUnsoldItem fromBytes(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {

            String item = in.readUTF();
            long expired = in.readLong();
            UUID sellerUuid = UUID.fromString(in.readUTF());
            UniqueName uniqueName = new CUniqueName(
                    in.readUTF(),
                    in.readInt(), // seed
                    in.readLong() // pos
            );
            long deleteVia = in.readLong();

            return new CUnsoldItem(
                    item, expired, sellerUuid, uniqueName, deleteVia
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
    public int hashCode() {
        return uniqueName.hashCode();
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
                sb.replace(sb.indexOf("{id}"), sb.indexOf("{id}") + "{id}".length(), String.valueOf(uniqueName.getKey()));
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
