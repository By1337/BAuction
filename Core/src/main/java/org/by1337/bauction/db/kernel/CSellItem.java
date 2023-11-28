package org.by1337.bauction.db.kernel;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.BLib;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.TagUtil;
import org.jetbrains.annotations.NotNull;

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
    final UUID uuid;
    final Material material;
    final int amount;
    final double priceForOne;
    final Set<String> sellFor;
    private transient ItemStack itemStack;

    public static CSellItemBuilder builder() {
        return new CSellItemBuilder();
    }

    public String toSql(String table) {
        return String.format(
                "INSERT INTO %s (uuid, seller_uuid, item, seller_name, price, sale_by_the_piece, tags, time_listed_for_sale, removal_date, material, amount, price_for_one, sell_for)" +
                        "VALUES('%s', '%s', '%s','%s', %s, %s, '%s', %s, %s, '%s', %s, %s, '%s')",
                table,          uuid, sellerUuid, item, sellerName, price, saleByThePiece, listToString(tags),
                timeListedForSale, removalDate, material.name(), amount, priceForOne, listToString(sellFor)
        );
    }

    public static CSellItem fromResultSet(ResultSet resultSet) throws SQLException {
        return CSellItem.builder()
                .uuid(UUID.fromString(resultSet.getString("uuid")))
                .sellerUuid(UUID.fromString(resultSet.getString("seller_uuid")))
                .item(resultSet.getString("item"))
                .sellerName(resultSet.getString("seller_name"))
                .price(resultSet.getDouble("price"))
                .saleByThePiece(resultSet.getBoolean("sale_by_the_piece"))
                .tags(new HashSet<>(Arrays.stream(resultSet.getString("tags").split(",")).toList()))
                .timeListedForSale(resultSet.getLong("time_listed_for_sale"))
                .removalDate(resultSet.getLong("removal_date"))
                .material(Material.valueOf(resultSet.getString("material")))
                .amount(resultSet.getInt("amount"))
                .priceForOne(resultSet.getDouble("price_for_one"))
                .sellFor(new HashSet<>(Arrays.stream(resultSet.getString("sell_for").split(",")).toList()))
                .build();
    }
    private static String listToString(Collection<?> collection) {
        StringBuilder sb = new StringBuilder();
        for (Object o : collection) {
            sb.append(o).append(",");
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public CSellItem(String item, String sellerName, UUID sellerUuid, double price, boolean saleByThePiece, Set<String> tags, long timeListedForSale, long removalDate, UUID uuid, Material material, int amount, double priceForOne, Set<String> sellFor, ItemStack itemStack) {
        this.item = item;
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        this.tags = tags;
        this.timeListedForSale = timeListedForSale;
        this.removalDate = removalDate;
        this.uuid = uuid;
        this.material = material;
        this.amount = amount;
        this.priceForOne = priceForOne;
        this.sellFor = sellFor;
        this.itemStack = itemStack;
    }

    public CSellItem(String item, String sellerName, UUID sellerUuid, double price, boolean saleByThePiece, Set<String> tags, long timeListedForSale, long removalDate, UUID uuid, Material material, int amount, double priceForOne, ItemStack itemStack) {
        this.item = item;
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        this.tags = tags;
        this.timeListedForSale = timeListedForSale;
        this.removalDate = removalDate;
        this.uuid = uuid;
        this.material = material;
        this.amount = amount;
        this.priceForOne = priceForOne;
        this.itemStack = itemStack;
        sellFor = new HashSet<>();
    }

    public CSellItem(String item, String sellerName, UUID sellerUuid,
                     double price, boolean saleByThePiece, Set<String> tags,
                     long timeListedForSale, long removalDate, UUID uuid, Material material,
                     int amount, double priceForOne) {
        this.item = item;
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        this.tags = tags;
        this.timeListedForSale = timeListedForSale;
        this.removalDate = removalDate;
        this.uuid = uuid;
        this.material = material;
        this.amount = amount;
        this.priceForOne = priceForOne;
        sellFor = new HashSet<>();
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
        this.uuid = UUID.randomUUID();
        this.material = material;
        this.amount = amount;
        priceForOne = price / amount;
        sellFor = new HashSet<>();
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
        this.uuid = UUID.randomUUID();
        material = itemStack.getType();
        amount = itemStack.getAmount();
        priceForOne = saleByThePiece ? price / amount : price;
        sellFor = new HashSet<>();
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
                item.getUuid(),
                item.getMaterial(),
                item.getAmount(),
                item.getPriceForOne()
        );
    }

    public ItemStack getItemStack() {
        if (itemStack == null) {
            itemStack = BLib.getApi().getItemStackSerialize().deserialize(item);
        }
        return itemStack;
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

    public UUID getUuid() {
        return uuid;
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
                ", uuid=" + uuid +
                ", material=" + material +
                ", amount=" + amount +
                ", priceForOne=" + priceForOne +
                ", sellFor=" + sellFor +
                '}';
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (true) {
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
                sb.replace(sb.indexOf("{sale_by_the_piece_format}"), sb.indexOf("{sale_by_the_piece_format}") + "{sale_by_the_piece_format}".length(), saleByThePiece ? "включена" : "отключена");
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
                sb.replace(sb.indexOf("{id}"), sb.indexOf("{id}") + "{id}".length(), String.valueOf(uuid));
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
                                Lang.getMessages(getMaterial().name().toLowerCase())
                );
                continue;
            }
            break;
        }
        return sb.toString();
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
        private UUID uuid;
        private Material material;
        private int amount;
        private double priceForOne;
        private ItemStack itemStack;
        private Set<String> sellFor;

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

        public CSellItemBuilder uuid(UUID uuid) {
            this.uuid = uuid;
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

        public CSellItemBuilder itemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public CSellItem build() {
            return new CSellItem(this.item, this.sellerName, this.sellerUuid, this.price, this.saleByThePiece, this.tags, this.timeListedForSale, this.removalDate, this.uuid, this.material, this.amount, this.priceForOne, this.itemStack);
        }

        public String toString() {
            return "CSellItem.CSellItemBuilder(item=" + this.item + ", sellerName=" + this.sellerName + ", sellerUuid=" + this.sellerUuid + ", price=" + this.price + ", saleByThePiece=" + this.saleByThePiece + ", tags=" + this.tags + ", timeListedForSale=" + this.timeListedForSale + ", removalDate=" + this.removalDate + ", uuid=" + this.uuid + ", material=" + this.material + ", amount=" + this.amount + ", priceForOne=" + this.priceForOne + ", itemStack=" + this.itemStack + ")";
        }

        public CSellItemBuilder sellFor(Set<String> sellFor) {
            this.sellFor = sellFor;
            return this;
        }
    }
}
