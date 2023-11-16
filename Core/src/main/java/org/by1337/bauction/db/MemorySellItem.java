package org.by1337.bauction.db;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.BLib;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.TagUtil;
import org.by1337.bauction.util.TimeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class MemorySellItem implements Placeholderable {
    private final String sellerName;
    private final UUID sellerUuid;
    private final double price;
    private final boolean saleByThePiece;
    private final Set<String> tags;
    private final long timeListedForSale;
    private final long removalDate;
    private final UUID uuid;
    private final Material material;
    private final int amount;
    private final double priceForOne;
    private final Set<String> sellFor;
    private final ItemStack itemStack;


    public MemorySellItem(String sellerName, UUID sellerUuid, double price, boolean saleByThePiece, Set<String> tags, long timeListedForSale, long removalDate, UUID uuid, Material material, int amount, double priceForOne, Set<String> sellFor, ItemStack itemStack) {
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

    public MemorySellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration) {
        this(seller, itemStack, price, saleDuration, true);
    }

    public MemorySellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration, boolean saleByThePiece) {
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
        this.itemStack = itemStack;
        sellFor = new HashSet<>();
        priceForOne = saleByThePiece ? price / amount : price;
    }

    public static MemorySellItemBuilder builder() {
        return new MemorySellItemBuilder();
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
            if (sb.indexOf("{id}") != -1) {
                sb.replace(sb.indexOf("{id}"), sb.indexOf("{id}") + "{id}".length(), String.valueOf(uuid));
                continue;
            }
            if (sb.indexOf("{sale_time}") != -1) {
                sb.replace(sb.indexOf("{sale_time}"), sb.indexOf("{sale_time}") + "{sale_time}".length(), String.valueOf(timeListedForSale / 1000));
                continue;
            }
            break;
        }
        return sb.toString();
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

    public Set<String> getSellFor() {
        return sellFor;
    }

    public ItemStack getItemStack() {
        return itemStack.clone();
    }

    public static class MemorySellItemBuilder {
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
        private Set<String> sellFor;
        private ItemStack itemStack;

        MemorySellItemBuilder() {
        }

        public MemorySellItemBuilder sellerName(String sellerName) {
            this.sellerName = sellerName;
            return this;
        }

        public MemorySellItemBuilder sellerUuid(UUID sellerUuid) {
            this.sellerUuid = sellerUuid;
            return this;
        }

        public MemorySellItemBuilder price(double price) {
            this.price = price;
            return this;
        }

        public MemorySellItemBuilder saleByThePiece(boolean saleByThePiece) {
            this.saleByThePiece = saleByThePiece;
            return this;
        }

        public MemorySellItemBuilder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        public MemorySellItemBuilder timeListedForSale(long timeListedForSale) {
            this.timeListedForSale = timeListedForSale;
            return this;
        }

        public MemorySellItemBuilder removalDate(long removalDate) {
            this.removalDate = removalDate;
            return this;
        }

        public MemorySellItemBuilder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public MemorySellItemBuilder material(Material material) {
            this.material = material;
            return this;
        }

        public MemorySellItemBuilder amount(int amount) {
            this.amount = amount;
            return this;
        }

        public MemorySellItemBuilder priceForOne(double priceForOne) {
            this.priceForOne = priceForOne;
            return this;
        }

        public MemorySellItemBuilder sellFor(Set<String> sellFor) {
            this.sellFor = sellFor;
            return this;
        }

        public MemorySellItemBuilder itemStack(ItemStack itemStack) {
            this.itemStack = itemStack;
            return this;
        }

        public MemorySellItem build() {
            return new MemorySellItem(this.sellerName, this.sellerUuid, this.price, this.saleByThePiece, this.tags, this.timeListedForSale, this.removalDate, this.uuid, this.material, this.amount, this.priceForOne, this.sellFor, this.itemStack);
        }
    }
}
