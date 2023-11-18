package org.by1337.bauction.datafix.db.json;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.Material;
import org.by1337.bauction.Main;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;

public class DBUpdateToV2 {

    public void update() {
        List<SellItem> items = load("items", new TypeToken<List<SellItem>>() {
        }.getType());
        Main.getMessage().logger("loaded %s items!", items.size());
        List<User> users = load("users", new TypeToken<List<User>>() {
        }.getType());
        Main.getMessage().logger("loaded %s users!", users.size());

        new File(Main.getInstance().getDataFolder() + "/items").delete();
        new File(Main.getInstance().getDataFolder() + "/users").delete();

        List<org.by1337.bauction.db.kernel.UnsoldItem> unsoldItems = new ArrayList<>();

        for (User user : users) {
            unsoldItems.addAll(user.unsoldItems.stream().map(UnsoldItem::convert).toList());
        }

        save0(items.stream().map(SellItem::convert).toList(), "sellItems", "sell-items-");
        save0(unsoldItems, "unsoldItems", "unsold-items-");
        save0(users.stream().map(User::convert).toList(), "users", "users-");

        Main.getMessage().logger("updated %s sellItems, %s users, %s unsoldItems", items.size(), users.size(), unsoldItems.size());
    }


    private <T> List<T> load(String dir, Type type) { //new TypeToken<>().getType()
        File home = new File(Main.getInstance().getDataFolder() + "/" + dir);
        List<T> out = new ArrayList<>();
        Gson gson = new Gson();
        try {
            if (!home.exists()) {
                home.mkdir();
            }
            for (File file : home.listFiles()) {
                try (FileReader reader = new FileReader(file)) {
                    out.addAll(gson.fromJson(reader, type));
                }
            }
        } catch (IOException e) {
            Main.getMessage().error("failed to save!", e);
        }
        return out;
    }

    private <T> void save0(List<T> list, String dir, String filePrefix) {
        File home = new File(Main.getInstance().getDataFolder() + "/" + dir);
        try {
            if (!home.exists()) {
                home.mkdir();
            }

            for (File file : home.listFiles()) {
                file.delete();
            }
            int max = 10000;
            int last = 0;

            int total = list.size();
            List<T> buffer = new ArrayList<>();
            Gson gson = new Gson();
            for (int i = 0; i < total; i++) {
                buffer.add(list.get(i));
                if (i - last >= max || i == total - 1) {
                    File file = new File(home + "/" + filePrefix + (last + 1) + "-" + (i) + ".json");
                    file.createNewFile();
                    try (FileWriter writer = new FileWriter(file)) {
                        gson.toJson(buffer, writer);
                        buffer.clear();
                        last = i;
                    }
                }
            }
        } catch (IOException e) {
            Main.getMessage().error("failed to save!", e);
        }
    }

    class SellItem {
        String item;
        String sellerName;
        UUID sellerUuid;
        double price;
        boolean saleByThePiece;
        Set<String> tags;
        long timeListedForSale;
        long removalDate;
        UUID uuid;
        Material material;
        int amount;
        double priceForOne;
        Set<String> sellFor = new HashSet<>();

        public org.by1337.bauction.db.kernel.SellItem convert() {
            return org.by1337.bauction.db.kernel.SellItem.builder()
                    .item(item)
                    .sellerName(sellerName)
                    .sellerUuid(sellerUuid)
                    .price(price)
                    .saleByThePiece(saleByThePiece)
                    .tags(tags)
                    .timeListedForSale(timeListedForSale)
                    .removalDate(removalDate)
                    .uuid(uuid)
                    .material(material)
                    .amount(amount)
                    .priceForOne(priceForOne)
                    .build();
        }
    }


    class UnsoldItem {
        String item;
        long expired;
        UUID owner;
        UUID uuid;
        long deleteVia;

        public org.by1337.bauction.db.kernel.UnsoldItem convert() {
            return new org.by1337.bauction.db.kernel.UnsoldItem(item, expired, owner, uuid, deleteVia);
        }
    }

    class User {
        String nickName;
        UUID uuid;
        List<UnsoldItem> unsoldItems = new ArrayList<>();
        List<UUID> itemForSale = new ArrayList<>();
        int dealCount;
        double dealSum;

        public org.by1337.bauction.db.kernel.User convert() {
            return new org.by1337.bauction.db.kernel.User(
                    nickName,
                    uuid,
                    unsoldItems.stream().map(i -> i.uuid).toList(),
                    itemForSale,
                    dealCount,
                    dealSum
            );
        }
    }
}
