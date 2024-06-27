package org.by1337.bauction.datafix.db;

import org.bukkit.Material;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.serialize.FileUtil;
import org.by1337.bauction.serialize.SerializeUtils;
import org.by1337.bauction.util.id.CUniqueName;
import org.by1337.bauction.api.util.UniqueName;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class DBUpdate109 {

    public void update() throws IOException {
        updateSellItems();
    }

    public void updateSellItems() throws IOException {
        File home = new File(Main.getInstance().getDataFolder() + "/data");
        if (!home.exists()) {
            home.mkdir();
        }
        File fItems = new File(home + "/items.bauc");
        List<SellItem> items;

        if (fItems.exists()) {
            items = FileUtil.read(fItems, DBUpdate109::fromBytesSellItem);
            if (items.isEmpty()) return;
        } else {
            return;
        }

        Main.getMessage().logger("loaded %s deprecated sell items!", items.size());
        FileUtil.write(fItems, items);
        Main.getMessage().logger("sell items updated!");
    }

    public static SellItem fromBytesSellItem(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {
            String item = in.readUTF();
            String sellerName = in.readUTF();
            UUID sellerUuid = SerializeUtils.readUUID(in);
            double price = in.readDouble();
            boolean saleByThePiece = in.readBoolean();
            Set<String> tags = new HashSet<>(SerializeUtils.readCollectionFromStream(in));
            long timeListedForSale = in.readLong();
            long removalDate = in.readLong();
            UniqueName uniqueName = new CUniqueName(
                    in.readUTF()
            );
            Material material = Material.valueOf(in.readUTF());
            int amount = in.readInt();
            double priceForOne = in.readDouble();
            Set<String> sellFor = new HashSet<>(SerializeUtils.readCollectionFromStream(in));

            return new SellItem(
                    item, sellerName, sellerUuid, price, saleByThePiece, tags, timeListedForSale, removalDate, uniqueName, material, amount, priceForOne, sellFor, null, Main.getServerId(), false
            );
        }
    }
}
