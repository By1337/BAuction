package org.by1337.bauction.db.kernel;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.BLib;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.serialize.SerializeUtils;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.TagUtil;
import org.by1337.bauction.util.UniqueName;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class CSellItem implements SellItem {
    final String item;
    final String sellerName;
    final UUID sellerUuid;
    final double price;
    final boolean saleByThePiece;
    final Set<String> tags;
    final long timeListedForSale;
    final long removalDate;
    final UniqueName uniqueName;
    final Material material;
    final int amount;
    final double priceForOne;
    final Set<String> sellFor;
    transient ItemStack itemStack;
    final String server;

    public static CSellItemBuilder builder() {
        return new CSellItemBuilder();
    }

    @Override
    public String toSql(String table) {
        return String.format(
                "INSERT INTO %s (uuid, seller_uuid, item, seller_name, price, sale_by_the_piece, tags, time_listed_for_sale, removal_date, material, amount, price_for_one, sell_for, server)" +
                        "VALUES('%s', '%s', '%s', '%s', %s, %s, '%s', %s, %s, '%s', %s, %s, '%s', '%s')",
                table, uniqueName.getKey(), sellerUuid, item, sellerName, price, saleByThePiece, listToString(tags), timeListedForSale, removalDate, material.name(), amount, priceForOne, listToString(sellFor), server
        );
    }

    public static CSellItem fromResultSet(ResultSet resultSet) throws SQLException {
        return CSellItem.builder()
                .uniqueName(new CUniqueName(resultSet.getString("uuid")))
                .sellerUuid(UUID.fromString(resultSet.getString("seller_uuid")))
                .item(resultSet.getString("item"))
                .sellerName(resultSet.getString("seller_name"))
                .price(resultSet.getDouble("price"))
                .saleByThePiece(resultSet.getBoolean("sale_by_the_piece"))
                .tags(new HashSet<>(Arrays.asList(resultSet.getString("tags").split(","))))
                .timeListedForSale(resultSet.getLong("time_listed_for_sale"))
                .removalDate(resultSet.getLong("removal_date"))
                .material(Material.valueOf(resultSet.getString("material")))
                .amount(resultSet.getInt("amount"))
                .priceForOne(resultSet.getDouble("price_for_one"))
                .sellFor(new HashSet<>(Arrays.asList(resultSet.getString("sell_for").split(","))))
                .server(resultSet.getString("server"))
                .build();
    }

    private static String listToString(Collection<? extends CharSequence> collection) {
        return String.join(",", collection);
    }

    public CSellItem(String item, String sellerName, UUID sellerUuid, double price, boolean saleByThePiece, Set<String> tags, long timeListedForSale, long removalDate, UniqueName uniqueName, Material material, int amount, double priceForOne, Set<String> sellFor, ItemStack itemStack, String server) {
        this.server = server;
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
        this.itemStack = itemStack;
    }

    public CSellItem(String item, String sellerName, UUID sellerUuid, double price, boolean saleByThePiece, Set<String> tags, long timeListedForSale, long removalDate, UniqueName uniqueName, Material material, int amount, double priceForOne, ItemStack itemStack) {
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
        this.itemStack = itemStack;
        sellFor = new HashSet<>();
        server = Main.getServerId();
    }

    public CSellItem(String item, String sellerName, UUID sellerUuid,
                     double price, boolean saleByThePiece, Set<String> tags,
                     long timeListedForSale, long removalDate, UniqueName uniqueName, Material material,
                     int amount, double priceForOne) {
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
        sellFor = new HashSet<>();
        server = Main.getServerId();
    }

    public CSellItem(@NotNull String item, @NotNull String sellerName, @NotNull UUID sellerUuid, double price, boolean saleByThePiece, @NotNull Set<String> tags, long saleDuration, @NotNull Material material, int amount) {
        this.item = item;
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        this.tags = Collections.unmodifiableSet(tags);
        this.timeListedForSale = System.currentTimeMillis();
        this.removalDate = System.currentTimeMillis() + saleDuration;
        this.uniqueName = Main.getUniqueNameGenerator().getNextCombination();
        this.material = material;
        this.amount = amount;
        priceForOne = price / amount;
        sellFor = new HashSet<>();
        server = Main.getServerId();
    }

    public CSellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration) {
        this(seller, itemStack, price, saleDuration, true);
    }

    public CSellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration, boolean saleByThePiece) {
        item = BLib.getApi().getItemStackSerialize().serialize(itemStack);
        sellerName = seller.getName();
        sellerUuid = seller.getUniqueId();
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        tags = Collections.unmodifiableSet(TagUtil.getTags(itemStack));
        timeListedForSale = System.currentTimeMillis();
        this.removalDate = System.currentTimeMillis() + saleDuration;
        this.uniqueName = Main.getUniqueNameGenerator().getNextCombination();
        material = itemStack.getType();
        amount = itemStack.getAmount();
        priceForOne = saleByThePiece ? price / amount : price;
        sellFor = new HashSet<>();
        server = Main.getServerId();
    }

    public CSellItem(String sellerName, UUID sellerUuid, @NotNull ItemStack itemStack, double price, long saleDuration, boolean saleByThePiece) {
        item = BLib.getApi().getItemStackSerialize().serialize(itemStack);
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        tags = Collections.unmodifiableSet(TagUtil.getTags(itemStack));
        timeListedForSale = System.currentTimeMillis();
        this.removalDate = System.currentTimeMillis() + saleDuration;
        this.uniqueName = Main.getUniqueNameGenerator().getNextCombination();
        material = itemStack.getType();
        amount = itemStack.getAmount();
        priceForOne = saleByThePiece ? price / amount : price;
        sellFor = new HashSet<>();
        server = Main.getServerId();
    }


    static CSellItem parse(SellItem item) {
        return new CSellItem(
                BLib.getApi().getItemStackSerialize().serialize(item.getItemStack()),
                item.getSellerName(),
                item.getSellerUuid(),
                item.getPrice(),
                item.isSaleByThePiece(),
                item.getTags(),
                item.getTimeListedForSale(),
                item.getRemovalDate(),
                item.getUniqueName(),
                item.getMaterial(),
                item.getAmount(),
                item.getPriceForOne(),
                new HashSet<>(),
                item.getItemStack(),
                item.getServer()
        );
    }

    public ItemStack getItemStack() {
        if (itemStack == null) {
            itemStack = BLib.getApi().getItemStackSerialize().deserialize(item);
        }
        return itemStack.clone();
    }

    public boolean isValid() {
        return item != null &&
                sellerName != null &&
                sellerUuid != null &&
                tags != null &&
                uniqueName != null &&
                material != null &&
                sellFor != null &&
                server != null;
    }

    @Override
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
            data.writeUTF(uniqueName.getKey());
            data.writeUTF(material.name());
            data.writeInt(amount);
            data.writeDouble(priceForOne);
            SerializeUtils.writeCollectionToStream(data, sellFor);
            data.writeUTF(server);
            data.flush();
            return out.toByteArray();
        }
    }

    public static CSellItem fromBytes(byte[] arr) throws IOException {
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
            String server = in.readUTF();

            return new CSellItem(
                    item, sellerName, sellerUuid, price, saleByThePiece, tags, timeListedForSale, removalDate, uniqueName, material, amount, priceForOne, sellFor, null, server
            );
        }
    }

    public String getItem() {
        return item;
    }

    public String getSellerName() {
        return sellerName;
    }

    public UUID getSellerUuid() {
        return sellerUuid;
    }

    public double getPrice() {
        return price;
    }

    public boolean isSaleByThePiece() {
        return saleByThePiece;
    }

    public Set<String> getTags() {
        return tags;
    }

    public long getTimeListedForSale() {
        return timeListedForSale;
    }

    public long getRemovalDate() {
        return removalDate;
    }

    public UniqueName getUniqueName() {
        return uniqueName;
    }

    public Material getMaterial() {
        return material;
    }

    public int getAmount() {
        return amount;
    }

    public double getPriceForOne() {
        return priceForOne;
    }

//    public Set<String> getSellFor() {
//        return sellFor;
//    }


    @Override
    public String toString() {
        return "CSellItem{" +
                "item='" + item + '\'' +
                ", sellerName='" + sellerName + '\'' +
                ", sellerUuid=" + sellerUuid +
                ", price=" + price +
                ", saleByThePiece=" + saleByThePiece +
                ", tags=" + tags +
                ", timeListedForSale=" + timeListedForSale +
                ", removalDate=" + removalDate +
                ", uniqueName=" + uniqueName +
                ", material=" + material +
                ", amount=" + amount +
                ", priceForOne=" + priceForOne +
                ", sellFor=" + sellFor +
                ", server='" + server + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CSellItem sellItem)) return false;
        return Double.compare(getPrice(), sellItem.getPrice()) == 0 && isSaleByThePiece() == sellItem.isSaleByThePiece() && getTimeListedForSale() == sellItem.getTimeListedForSale() && getRemovalDate() == sellItem.getRemovalDate() && getAmount() == sellItem.getAmount() && Double.compare(getPriceForOne(), sellItem.getPriceForOne()) == 0 && Objects.equals(getItem(), sellItem.getItem()) && Objects.equals(getSellerName(), sellItem.getSellerName()) && Objects.equals(getSellerUuid(), sellItem.getSellerUuid()) && Objects.equals(getTags(), sellItem.getTags()) && Objects.equals(getUniqueName(), sellItem.getUniqueName()) && getMaterial() == sellItem.getMaterial() && Objects.equals(sellFor, sellItem.sellFor) && Objects.equals(getItemStack(), sellItem.getItemStack());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(uniqueName.getKey().toCharArray());
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (true) {
            if (sb.indexOf("{seller_uuid}") != -1) {
                sb.replace(sb.indexOf("{seller_uuid}"), sb.indexOf("{seller_uuid}") + "{seller_uuid}".length(), sellerUuid.toString());
                continue;
            }
            if (sb.indexOf("{seller_name}") != -1) {
                sb.replace(sb.indexOf("{seller_name}"), sb.indexOf("{seller_name}") + "{seller_name}".length(), sellerName);
                continue;
            }
            if (sb.indexOf("{price}") != -1) {
                sb.replace(sb.indexOf("{price}"), sb.indexOf("{price}") + "{price}".length(), NumberUtil.format(price));
                continue;
            }
            if (sb.indexOf("{sale_by_the_piece}") != -1) {
                sb.replace(sb.indexOf("{sale_by_the_piece}"), sb.indexOf("{sale_by_the_piece}") + "{sale_by_the_piece}".length(), String.valueOf(saleByThePiece));
                continue;
            }
            if (sb.indexOf("{sale_by_the_piece_format}") != -1) {
                sb.replace(sb.indexOf("{sale_by_the_piece_format}"), sb.indexOf("{sale_by_the_piece_format}") + "{sale_by_the_piece_format}".length(), saleByThePiece ?
                        Lang.getMessage("sale-by-the-piece-format-on") : Lang.getMessage("sale-by-the-piece-format-off")
                );
                continue;
            }
            if (sb.indexOf("{expires}") != -1) {
                sb.replace(sb.indexOf("{expires}"), sb.indexOf("{expires}") + "{expires}".length(), Main.getTimeUtil().getFormat(removalDate));
                continue;
            }
            if (sb.indexOf("{price_for_one}") != -1) {
                sb.replace(sb.indexOf("{price_for_one}"), sb.indexOf("{price_for_one}") + "{price_for_one}".length(), NumberUtil.format(priceForOne));
                continue;
            }
            if (sb.indexOf("{material}") != -1) {
                sb.replace(sb.indexOf("{material}"), sb.indexOf("{material}") + "{material}".length(), String.valueOf(material));
                continue;
            }
            if (sb.indexOf("{amount}") != -1) {
                sb.replace(sb.indexOf("{amount}"), sb.indexOf("{amount}") + "{amount}".length(), String.valueOf(amount));
                continue;
            }
            if (sb.indexOf("{id}") != -1) {
                sb.replace(sb.indexOf("{id}"), sb.indexOf("{id}") + "{id}".length(), String.valueOf(uniqueName.getKey()));
                continue;
            }
            if (sb.indexOf("{sale_time}") != -1) {
                sb.replace(sb.indexOf("{sale_time}"), sb.indexOf("{sale_time}") + "{sale_time}".length(), String.valueOf(timeListedForSale / 1000));
                continue;
            }
            if (sb.indexOf("{item_name}") != -1) {
                sb.replace(sb.indexOf("{item_name}"), sb.indexOf("{item_name}") + "{item_name}".length(),
                        getItemStack().getItemMeta() != null && getItemStack().getItemMeta().hasDisplayName() ?
                                getItemStack().getItemMeta().getDisplayName() :
                                Lang.getMessage(getMaterial().name().toLowerCase())
                );
                continue;
            }
            break;
        }
        return sb.toString();
    }

    @Override
    public String getServer() {
        return server;
    }

    public static class CSellItemBuilder {
        private String item;
        private String sellerName;
        private UUID sellerUuid;
        private double price;
        private boolean saleByThePiece;
        private Set<String> tags;
        private long timeListedForSale;
        private long removalDate;
        private UniqueName uniqueName;
        private Material material;
        private int amount;
        private double priceForOne;
        private Set<String> sellFor;
        private ItemStack itemStack;
        private String server;

        CSellItemBuilder() {
        }

        public CSellItemBuilder item(String item) {
            this.item = item;
            return this;
        }

        public CSellItemBuilder sellerName(String sellerName) {
            this.sellerName = sellerName;
            return this;
        }

        public CSellItemBuilder server(String server) {
            this.server = server;
            return this;
        }

        public CSellItemBuilder sellerUuid(UUID sellerUuid) {
            this.sellerUuid = sellerUuid;
            return this;
        }

        public CSellItemBuilder price(double price) {
            this.price = price;
            return this;
        }

        public CSellItemBuilder saleByThePiece(boolean saleByThePiece) {
            this.saleByThePiece = saleByThePiece;
            return this;
        }

        public CSellItemBuilder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public CSellItemBuilder timeListedForSale(long timeListedForSale) {
            this.timeListedForSale = timeListedForSale;
            return this;
        }

        public CSellItemBuilder removalDate(long removalDate) {
            this.removalDate = removalDate;
            return this;
        }

        public CSellItemBuilder uniqueName(UniqueName uniqueName) {
            this.uniqueName = uniqueName;
            return this;
        }

        public CSellItemBuilder material(Material material) {
            this.material = material;
            return this;
        }

        public CSellItemBuilder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public CSellItemBuilder priceForOne(double priceForOne) {
            this.priceForOne = priceForOne;
            return this;
        }

        public CSellItemBuilder sellFor(Set<String> sellFor) {
            this.sellFor = sellFor;
            return this;
        }

        public CSellItemBuilder itemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public CSellItem build() {
            return new CSellItem(this.item, this.sellerName, this.sellerUuid, this.price, this.saleByThePiece, this.tags, this.timeListedForSale, this.removalDate, this.uniqueName, this.material, this.amount, this.priceForOne, this.sellFor, this.itemStack, server);
        }

    }

}
