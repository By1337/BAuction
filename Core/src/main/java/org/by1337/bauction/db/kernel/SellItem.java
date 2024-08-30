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
import org.by1337.blib.nbt.CompressedNBT;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.impl.ByteArrNBT;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SellItem extends Placeholder implements ItemHolder {
    public static final Codec<SellItem> CODEC = new SellItemCodec();
    public final NBT item;
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

    public SellItem(NBT item, String sellerName, UUID sellerUuid,
                    double price, boolean saleByThePiece, Set<String> tags,
                    long timeListedForSale, long removalDate, long id,
                    Material material, int amount, double priceForOne,
                    @Nullable ItemStack itemStack, String server) {
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
        init();
    }

    public SellItem(NBT item, String sellerName, UUID sellerUuid,
                    double price, boolean saleByThePiece, Set<String> tags,
                    long timeListedForSale, long removalDate, long id,
                    Material material, int amount, double priceForOne, @Nullable ItemStack itemStack) {
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
        server = Main.getServerId();
        init();
    }

    public SellItem(NBT item, String sellerName, UUID sellerUuid,
                    double price, boolean saleByThePiece, Set<String> tags,
                    long timeListedForSale, long removalDate, long id, Material material,
                    int amount, double priceForOne) {
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
        server = Main.getServerId();
        init();
    }

    public SellItem(@NotNull NBT item, @NotNull String sellerName, @NotNull UUID sellerUuid,
                    double price, boolean saleByThePiece, @NotNull Set<String> tags, long saleDuration,
                    @NotNull Material material, int amount) {
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
        priceForOne = price / amount;
        server = Main.getServerId();
        init();
    }

    public SellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration) {
        this(seller, itemStack, price, saleDuration, true);
    }

    public SellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration, boolean saleByThePiece) {
        item = serializeItemStack(itemStack);
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

    public static NBT serializeItemStack(ItemStack itemStack) {
        CompoundTag tag = BLib.getApi().getParseCompoundTag().copy(itemStack);
        if (tag.getSizeInBytes() > Main.getCfg().getCompressIfMoreThan()) {
            return tag.getAsCompressedNBT();
        } else {
            return tag;
        }
    }

    public SellItem(String sellerName, UUID sellerUuid, @NotNull ItemStack itemStack, double price, long saleDuration, boolean saleByThePiece) {
        item = serializeItemStack(itemStack);
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
            if (item instanceof ByteArrNBT arrNBT) {
                CompoundTag tag = (CompoundTag) new CompressedNBT(arrNBT.getValue()).decompress();
                itemStack = BLib.getApi().getParseCompoundTag().create(tag);
            } else {
                itemStack = BLib.getApi().getParseCompoundTag().create((CompoundTag) item);
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

    public NBT getItem() {
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
    public String compactToString(){
        return "SellItem{" +
               "sellerName='" + sellerName + '\'' +
               ", sellerUuid=" + sellerUuid +
               ", price=" + price +
               ", saleByThePiece=" + saleByThePiece +
               ", tags=" + tags +
               ", removalDate=" + removalDate +
               ", id=" + id +
               ", material=" + material +
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
                Objects.equals(server, sellItem.server);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, sellerName, sellerUuid, price, saleByThePiece, tags, timeListedForSale, removalDate, id, material, amount, priceForOne, server);
    }


    public static class SellItemBuilder {
        private NBT item;
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
        private ItemStack itemStack;
        private String server;

        SellItemBuilder() {
        }

        public SellItemBuilder copy(SellItem sellItem) {
            item = sellItem.item;
            sellerName = sellItem.sellerName;
            sellerUuid = sellItem.sellerUuid;
            price = sellItem.price;
            saleByThePiece = sellItem.saleByThePiece;
            tags = sellItem.tags;
            timeListedForSale = sellItem.timeListedForSale;
            removalDate = sellItem.removalDate;
            id = sellItem.id;
            material = sellItem.material;
            amount = sellItem.amount;
            priceForOne = sellItem.priceForOne;
            itemStack = sellItem.itemStack;
            server = sellItem.server;
            return this;
        }

        public SellItemBuilder item(NBT item) {
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

        public SellItemBuilder itemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public SellItem build() {
            return new SellItem(this.item, this.sellerName, this.sellerUuid, this.price, this.saleByThePiece, this.tags, this.timeListedForSale, this.removalDate, this.id, this.material, this.amount, this.priceForOne, this.itemStack, server);
        }

    }
}
