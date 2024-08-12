package org.by1337.bauction.db.kernel;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.ItemHolder;
import org.by1337.bauction.db.io.codec.Codec;
import org.by1337.bauction.db.io.codec.SellItemCodec;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.auction.TagUtil;
import org.by1337.bauction.util.common.NumberUtil;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.util.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SellItem extends Placeholder implements ItemHolder {
    public static final Codec<SellItem> CODEC = new SellItemCodec();
    public final String item;
    public final String sellerName;
    public final UUID sellerUuid;
    public final double price;
    public final boolean saleByThePiece;
    public final Set<String> tags;
    public final long timeListedForSale;
    public final long removalDate;
    public final long id;
    public final Material material;
    public final int amount;
    public final transient double priceForOne;
    @Nullable
    public transient ItemStack itemStack;
    public final String server;
    public final boolean compressed;

    public static SellItemBuilder builder() {
        return new SellItemBuilder();
    }

    @Deprecated(forRemoval = true)
    public String toSql(String table) {
        return null;
    }

    @Deprecated(forRemoval = true)
    public static SellItem fromResultSet(ResultSet resultSet) throws SQLException {
        return null;
    }

    public SellItem(String item, String sellerName, UUID sellerUuid,
                    double price, boolean saleByThePiece, Set<String> tags,
                    long timeListedForSale, long removalDate, long id,
                    Material material, int amount, double priceForOne,
                    @Nullable ItemStack itemStack, String server, boolean compressed) {
        this.server = server;
        this.item = item;
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        this.tags = tags;
        this.timeListedForSale = timeListedForSale;
        this.removalDate = removalDate;
        this.id = id;
        this.material = material;
        this.amount = amount;
        this.priceForOne = priceForOne;
        this.itemStack = itemStack;
        this.compressed = compressed;
        init();
    }

    public SellItem(String item, String sellerName, UUID sellerUuid,
                    double price, boolean saleByThePiece, Set<String> tags,
                    long timeListedForSale, long removalDate, long id,
                    Material material, int amount, double priceForOne, @Nullable ItemStack itemStack, boolean compressed) {
        this.item = item;
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        this.tags = tags;
        this.timeListedForSale = timeListedForSale;
        this.removalDate = removalDate;
        this.id = id;
        this.material = material;
        this.amount = amount;
        this.priceForOne = priceForOne;
        this.itemStack = itemStack;
        this.compressed = compressed;
        server = Main.getServerId();
        init();
    }

    public SellItem(String item, String sellerName, UUID sellerUuid,
                    double price, boolean saleByThePiece, Set<String> tags,
                    long timeListedForSale, long removalDate, long id, Material material,
                    int amount, double priceForOne, boolean compressed) {
        this.item = item;
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        this.tags = tags;
        this.timeListedForSale = timeListedForSale;
        this.removalDate = removalDate;
        this.id = id;
        this.material = material;
        this.amount = amount;
        this.priceForOne = priceForOne;
        this.compressed = compressed;
        server = Main.getServerId();
        init();
    }

    public SellItem(@NotNull String item, @NotNull String sellerName, @NotNull UUID sellerUuid,
                    double price, boolean saleByThePiece, @NotNull Set<String> tags, long saleDuration,
                    @NotNull Material material, int amount, boolean compressed) {
        this.item = item;
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        this.tags = Collections.unmodifiableSet(tags);
        this.timeListedForSale = System.currentTimeMillis();
        this.removalDate = System.currentTimeMillis() + saleDuration;
        this.id = Main.getUniqueIdGenerator().nextId();
        this.material = material;
        this.amount = amount;
        this.compressed = compressed;
        priceForOne = price / amount;
        server = Main.getServerId();
        init();
    }

    public SellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration) {
        this(seller, itemStack, price, saleDuration, true);
    }

    public SellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration, boolean saleByThePiece) {
        var pair = serializeItemStack(itemStack);
        item = pair.getLeft();
        compressed = pair.getRight();
        sellerName = seller.getName();
        sellerUuid = seller.getUniqueId();
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        tags = Collections.unmodifiableSet(TagUtil.getTags(itemStack));
        timeListedForSale = System.currentTimeMillis();
        this.removalDate = System.currentTimeMillis() + saleDuration;
        this.id = Main.getUniqueIdGenerator().nextId();
        material = itemStack.getType();
        amount = itemStack.getAmount();
        priceForOne = price / amount;
        server = Main.getServerId();
        init();
    }

    public static Pair<String, Boolean> serializeItemStack(ItemStack itemStack) {
        var temp = BLib.getApi().getItemStackSerialize().serialize(itemStack);
        if (temp.getBytes().length > Main.getCfg().getCompressIfMoreThan()) {
            return Pair.of(BLib.getApi().getItemStackSerialize().serializeAndCompress(itemStack), true);
        } else {
            return Pair.of(temp, false);
        }
    }

    public SellItem(String sellerName, UUID sellerUuid, @NotNull ItemStack itemStack, double price, long saleDuration, boolean saleByThePiece) {
        var pair = serializeItemStack(itemStack);
        item = pair.getLeft();
        compressed = pair.getRight();
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        tags = Collections.unmodifiableSet(TagUtil.getTags(itemStack));
        timeListedForSale = System.currentTimeMillis();
        this.removalDate = System.currentTimeMillis() + saleDuration;
        this.id = Main.getUniqueIdGenerator().nextId();
        material = itemStack.getType();
        amount = itemStack.getAmount();
        priceForOne = price / amount;
        server = Main.getServerId();
        init();
    }

    private void init() {
        registerPlaceholder("{seller_uuid}", sellerUuid::toString);
        registerPlaceholder("{seller_name}", () -> sellerName);
        registerPlaceholder("{price}", () -> NumberUtil.format(price));
        registerPlaceholder("{price_format}", () -> NumberUtil.formatNumberWithThousandsSeparator(price));
        registerPlaceholder("{sale_by_the_piece}", () -> saleByThePiece);
        registerPlaceholder("{sale_by_the_piece_format}", () -> saleByThePiece ?
                Lang.getMessage("sale-by-the-piece-format-on") : Lang.getMessage("sale-by-the-piece-format-off"));
        registerPlaceholder("{expires}", () -> Main.getTimeUtil().getFormat(removalDate));
        registerPlaceholder("{price_for_one}", () -> NumberUtil.format(priceForOne));
        registerPlaceholder("{price_for_one_format}", () -> NumberUtil.formatNumberWithThousandsSeparator(priceForOne));
        registerPlaceholder("{material}", () -> material);
        registerPlaceholder("{amount}", () -> amount);
        registerPlaceholder("{id}", () -> id);
        registerPlaceholder("{sale_time}", () -> timeListedForSale / 1000);
        registerPlaceholder("{item_name}", () -> getItemStack().getItemMeta() != null && getItemStack().getItemMeta().hasDisplayName() ?
                getItemStack().getItemMeta().getDisplayName() :
                Lang.getMessage(getMaterial().name().toLowerCase()));
        registerPlaceholder("{seller_is_online}", () -> Bukkit.getPlayer(sellerUuid) != null);
        registerPlaceholder("{seller_is_online_format}", () ->
                (Bukkit.getPlayer(sellerUuid) != null) ?
                        Lang.getMessage("online-seller") : Lang.getMessage("offline-seller")
        );
    }

    @Deprecated(forRemoval = true)
    static SellItem parse(SellItem item) {
        return item;
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
               sellerName != null &&
               sellerUuid != null &&
               tags != null &&
               material != null &&
               server != null;
    }

    public boolean hasAllTags(Category category) {
        return tags.containsAll(category.tags());
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

    public long getId() {
        return id;
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


    @Override
    public String toString() {
        return "SellItem{" +
               "item='" + item + '\'' +
               ", sellerName='" + sellerName + '\'' +
               ", sellerUuid=" + sellerUuid +
               ", price=" + price +
               ", saleByThePiece=" + saleByThePiece +
               ", tags=" + tags +
               ", timeListedForSale=" + timeListedForSale +
               ", removalDate=" + removalDate +
               ", id=" + id +
               ", material=" + material +
               ", amount=" + amount +
               ", priceForOne=" + priceForOne +
               ", server='" + server + '\'' +
               '}';
    }

    public String getServer() {
        return server;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SellItem sellItem = (SellItem) o;
        return
                Double.compare(price, sellItem.price) == 0 &&
                saleByThePiece == sellItem.saleByThePiece &&
                timeListedForSale == sellItem.timeListedForSale &&
                removalDate == sellItem.removalDate &&
                amount == sellItem.amount &&
                Double.compare(priceForOne, sellItem.priceForOne) == 0 &&
                Objects.equals(item, sellItem.item) &&
                Objects.equals(sellerName, sellItem.sellerName) &&
                Objects.equals(sellerUuid, sellItem.sellerUuid) &&
                Objects.equals(tags, sellItem.tags) &&
                id == sellItem.id &&
                material == sellItem.material &&
                Objects.equals(server, sellItem.server) &&
                compressed == sellItem.compressed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, sellerName, sellerUuid, price, saleByThePiece, tags, timeListedForSale, removalDate, id, material, amount, priceForOne, server, compressed);
    }


    public static class SellItemBuilder {
        private String item;
        private String sellerName;
        private UUID sellerUuid;
        private double price;
        private boolean saleByThePiece;
        private Set<String> tags;
        private long timeListedForSale;
        private long removalDate;
        private long id;
        private Material material;
        private int amount;
        private double priceForOne;
        private Set<String> sellFor;
        private ItemStack itemStack;
        private String server;
        private boolean compressed;

        SellItemBuilder() {
        }

        public SellItemBuilder item(String item) {
            this.item = item;
            return this;
        }

        public SellItemBuilder sellerName(String sellerName) {
            this.sellerName = sellerName;
            return this;
        }

        public SellItemBuilder server(String server) {
            this.server = server;
            return this;
        }

        public SellItemBuilder sellerUuid(UUID sellerUuid) {
            this.sellerUuid = sellerUuid;
            return this;
        }

        public SellItemBuilder price(double price) {
            this.price = price;
            return this;
        }

        public SellItemBuilder saleByThePiece(boolean saleByThePiece) {
            this.saleByThePiece = saleByThePiece;
            return this;
        }

        public SellItemBuilder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public SellItemBuilder timeListedForSale(long timeListedForSale) {
            this.timeListedForSale = timeListedForSale;
            return this;
        }

        public SellItemBuilder removalDate(long removalDate) {
            this.removalDate = removalDate;
            return this;
        }

        public SellItemBuilder id(long id) {
            this.id = id;
            return this;
        }

        public SellItemBuilder material(Material material) {
            this.material = material;
            return this;
        }

        public SellItemBuilder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public SellItemBuilder priceForOne(double priceForOne) {
            this.priceForOne = priceForOne;
            return this;
        }

        public SellItemBuilder sellFor(Set<String> sellFor) {
            this.sellFor = sellFor;
            return this;
        }

        public SellItemBuilder itemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public SellItemBuilder compressed(boolean compressed) {
            this.compressed = compressed;
            return this;
        }

        public SellItem build() {
            return new SellItem(this.item, this.sellerName, this.sellerUuid, this.price, this.saleByThePiece, this.tags, this.timeListedForSale, this.removalDate, this.id, this.material, this.amount, this.priceForOne, this.itemStack, server, compressed);
        }

    }

    public boolean isCompressed() {
        return compressed;
    }
}
