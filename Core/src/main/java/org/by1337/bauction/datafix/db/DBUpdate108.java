package org.by1337.bauction.datafix.db;

import org.bukkit.Material;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.CSellItem;
import org.by1337.bauction.db.kernel.CUnsoldItem;
import org.by1337.bauction.db.kernel.CUser;
import org.by1337.bauction.serialize.FileUtil;
import org.by1337.bauction.serialize.SerializeUtils;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.api.util.UniqueName;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class DBUpdate108 {

    public void update() throws IOException {
        updateUsers();
        updateSellItems();
        updateUnsoldItems();
    }

    public void updateSellItems() throws IOException {
        File home = new File(Main.getInstance().getDataFolder() + "/data");
        if (!home.exists()) {
            home.mkdir();
        }
        File fItems = new File(home + "/items.bauc");
        List<CSellItem> items;

        if (fItems.exists()) {
            items = FileUtil.read(fItems, DBUpdate108::fromBytesSellItem);
            if (items.isEmpty()) return;
        } else {
            return;
        }

        Main.getMessage().logger("loaded %s deprecated sell items!", items.size());
        FileUtil.write(fItems, items);
        Main.getMessage().logger("sell items updated!");
    }

    public void updateUnsoldItems() throws IOException {
        File home = new File(Main.getInstance().getDataFolder() + "/data");
        File fUnsoldItems = new File(home + "/unsoldItems.bauc");
        List<CUnsoldItem> unsoldItems;

        if (fUnsoldItems.exists()) {
            unsoldItems = FileUtil.read(fUnsoldItems, DBUpdate108::fromBytesUnsoldItem);
            if (unsoldItems.isEmpty()) return;
        } else {
            return;
        }

        Main.getMessage().logger("loaded %s deprecated unsold items!", unsoldItems.size());
        FileUtil.write(fUnsoldItems, unsoldItems);
        Main.getMessage().logger("unsold items updated!");
    }

    public void updateUsers() throws IOException {
        File home = new File(Main.getInstance().getDataFolder() + "/data");
        File fUsers = new File(home + "/users.bauc");
        List<CUser> users;

        if (fUsers.exists()) {
            users = FileUtil.read(fUsers, DBUpdate108::fromBytesUser);
            if (users.isEmpty()) return;
        } else {
            return;
        }

        Main.getMessage().logger("loaded %s deprecated users!", users.size());
        FileUtil.write(fUsers, users);
        Main.getMessage().logger("users updated!");
    }

    public static CUser fromBytesUser(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {
            String nickName = in.readUTF();
            UUID uuid = UUID.fromString(in.readUTF());
            int dealCount = in.readInt();
            double dealSum = in.readDouble();

            return new CUser(
                    nickName, uuid, dealCount, dealSum
            );
        }
    }

    public static CSellItem fromBytesSellItem(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {
            String item = in.readUTF();
            String sellerName = in.readUTF();
            UUID sellerUuid = UUID.fromString(in.readUTF());
            double price = in.readDouble();
            boolean saleByThePiece = in.readBoolean();
            Set<String> tags = new HashSet<>(SerializeUtils.readCollectionFromStream(in));
            long timeListedForSale = in.readLong();
            long removalDate = in.readLong();
            UniqueName uniqueName = new CUniqueName(
                    in.readUTF(),
                    in.readInt(), // seed
                    in.readLong() // pos
            );
            Material material = Material.valueOf(in.readUTF());
            int amount = in.readInt();
            double priceForOne = in.readDouble();
            Set<String> sellFor = new HashSet<>(SerializeUtils.readCollectionFromStream(in));

            return new CSellItem(
                    item, sellerName, sellerUuid, price, saleByThePiece, tags, timeListedForSale, removalDate, uniqueName, material, amount, priceForOne, sellFor, null, Main.getServerId(),false
            );
        }
    }

    public static CUnsoldItem fromBytesUnsoldItem(byte[] arr) throws IOException {
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
                    item, expired, sellerUuid, uniqueName, deleteVia, false
            );
        }
    }
}
