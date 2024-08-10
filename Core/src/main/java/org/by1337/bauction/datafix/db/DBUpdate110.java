package org.by1337.bauction.datafix.db;

import org.bukkit.Material;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.serialize.SerializableToByteArray;
import org.by1337.bauction.serialize.FileUtil;
import org.by1337.bauction.serialize.SerializeUtils;

import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DBUpdate110 {

    public void update() throws IOException {
        updateSellItems();
        updateUnsoldItems();
    }


    private void updateUnsoldItems() throws IOException {
        File home = new File(Main.getInstance().getDataFolder() + "/data");
        if (!home.exists()) {
            home.mkdir();
        }
        File fItems = new File(home + "/unsoldItems.bauc");
        List<UnsoldItem_> items;

        if (fItems.exists()) {
            items = FileUtil.read(fItems, DBUpdate110::fromBytesUnsoldItem);
            if (items.isEmpty()) return;
        } else {
            return;
        }

        Main.getMessage().logger("loaded %s deprecated unsold items!", items.size());
        FileUtil.write(fItems, items);
        Main.getMessage().logger("unsold items updated!");
    }

    private void updateSellItems() throws IOException {
        File home = new File(Main.getInstance().getDataFolder() + "/data");
        if (!home.exists()) {
            home.mkdir();
        }
        File fItems = new File(home + "/items.bauc");
        List<SellItem_> items;

        if (fItems.exists()) {
            items = FileUtil.read(fItems, DBUpdate110::fromBytesSellItem);
            if (items.isEmpty()) return;
        } else {
            return;
        }

        Main.getMessage().logger("loaded %s deprecated sell items!", items.size());
        FileUtil.write(fItems, items);
        Main.getMessage().logger("sell items updated!");
    }

    private static UnsoldItem_ fromBytesUnsoldItem(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {

            String item = in.readUTF();
            long expired = in.readLong();
            UUID sellerUuid = SerializeUtils.readUUID(in);
            String uniqueName = in.readUTF();
            long deleteVia = in.readLong();

            return new UnsoldItem_(
                    item, expired, sellerUuid, uniqueName, deleteVia, false
            );
        }
    }

    private static SellItem_ fromBytesSellItem(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {
            String item = in.readUTF();
            String sellerName = in.readUTF();
            UUID sellerUuid = SerializeUtils.readUUID(in);
            double price = in.readDouble();
            boolean saleByThePiece = in.readBoolean();
            Set<String> tags = new HashSet<>(SerializeUtils.readCollectionFromStream(in));
            long timeListedForSale = in.readLong();
            long removalDate = in.readLong();
            String uniqueName = in.readUTF();
            Material material = Material.valueOf(in.readUTF());
            int amount = in.readInt();
            double priceForOne = in.readDouble();
            Set<String> sellFor = new HashSet<>(SerializeUtils.readCollectionFromStream(in));
            String server = in.readUTF();

            return new SellItem_(
                    item, sellerName, sellerUuid, price, saleByThePiece, tags, timeListedForSale, removalDate, uniqueName, material, amount, priceForOne, sellFor, server, false
            );
        }
    }

    private static class SellItem_ implements SerializableToByteArray {
        final String item;
        final String sellerName;
        final UUID sellerUuid;
        final double price;
        final boolean saleByThePiece;
        final Set<String> tags;
        final long timeListedForSale;
        final long removalDate;
        final String uniqueName;
        final Material material;
        final int amount;
        final double priceForOne;
        final Set<String> sellFor;
        final String server;
        final boolean compressed;

        public SellItem_(String item, String sellerName, UUID sellerUuid, double price, boolean saleByThePiece, Set<String> tags, long timeListedForSale, long removalDate, String uniqueName, Material material, int amount, double priceForOne, Set<String> sellFor, String server, boolean compressed) {
            this.item = item;
            this.sellerName = sellerName;
            this.sellerUuid = sellerUuid;
            this.price = price;
            this.saleByThePiece = saleByThePiece;
            this.tags = tags;
            this.timeListedForSale = timeListedForSale;
            this.removalDate = removalDate;
            this.uniqueName = uniqueName;
            this.material = material;
            this.amount = amount;
            this.priceForOne = priceForOne;
            this.sellFor = sellFor;
            this.server = server;
            this.compressed = compressed;
        }

        public byte[] getBytes() throws IOException {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 DataOutputStream data = new DataOutputStream(out)) {
                data.writeUTF(item);
                data.writeUTF(sellerName);
                SerializeUtils.writeUUID(sellerUuid, data);
                data.writeDouble(price);
                data.writeBoolean(saleByThePiece);
                SerializeUtils.writeCollectionToStream(data, tags);
                data.writeLong(timeListedForSale);
                data.writeLong(removalDate);
                data.writeUTF(uniqueName);
                data.writeUTF(material.name());
                data.writeInt(amount);
                data.writeDouble(priceForOne);
                SerializeUtils.writeCollectionToStream(data, sellFor);
                data.writeUTF(server);
                data.writeBoolean(compressed);
                data.flush();
                return out.toByteArray();
            }
        }
    }

    private static class UnsoldItem_ implements SerializableToByteArray {
        final String item;
        final long expired;
        final UUID sellerUuid;
        final String uniqueName;
        final long deleteVia;
        final boolean compressed;

        public UnsoldItem_(String item, long expired, UUID sellerUuid, String uniqueName, long deleteVia, boolean compressed) {
            this.item = item;
            this.expired = expired;
            this.sellerUuid = sellerUuid;
            this.uniqueName = uniqueName;
            this.deleteVia = deleteVia;
            this.compressed = compressed;
        }

        @Override
        public byte[] getBytes() throws IOException {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 DataOutputStream data = new DataOutputStream(out)) {
                data.writeUTF(item);
                data.writeLong(expired);
                SerializeUtils.writeUUID(sellerUuid, data);
                data.writeUTF(uniqueName);
                data.writeLong(deleteVia);
                data.writeBoolean(compressed);
                data.flush();
                return out.toByteArray();
            }
        }
    }

}
