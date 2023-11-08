package org.by1337.bauction;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.BLib;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.TagUtil;
import org.by1337.bauction.util.TimeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class SellItem implements Placeholderable {
    private final String item;
    private final String sellerName;
    private final UUID sellerUuid;
    private final double price;
    private final boolean saleByThePiece;
    private final HashSet<String> tags;
    private final long timeListedForSale;
    private final long removalDate;
    private final UUID uuid;
    private final Material material;
    private final int amount;
    private final double priceForOne;
    private final List<String> sellFor = new ArrayList<>();
    private transient ItemStack itemStack;

    public SellItem(@NotNull String item, @NotNull String sellerName, @NotNull UUID sellerUuid, double price, boolean saleByThePiece, @NotNull HashSet<String> tags, long saleDuration, @NotNull Material material, int amount) {
        this.item = item;
        this.sellerName = sellerName;
        this.sellerUuid = sellerUuid;
        this.price = price;
        this.saleByThePiece = saleByThePiece;
        this.tags = tags;
        this.timeListedForSale = System.currentTimeMillis();
        this.removalDate = System.currentTimeMillis() + saleDuration;
        this.uuid = UUID.randomUUID();
        this.material = material;
        this.amount = amount;
        priceForOne = price / amount;
    }

    public SellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration) {
        item = BLib.getApi().getItemStackSerialize().serialize(itemStack);
        sellerName = seller.getName();
        sellerUuid = seller.getUniqueId();
        this.price = price;
        saleByThePiece = false;
        tags = TagUtil.getTags(itemStack);
        timeListedForSale = System.currentTimeMillis();
        this.removalDate = System.currentTimeMillis() + saleDuration;
        this.uuid = UUID.randomUUID();
        material = itemStack.getType();
        amount = itemStack.getAmount();
        priceForOne = price;
        this.itemStack = itemStack;
    }

    @NotNull
    public ItemStack getItemStack() {
        if (itemStack == null) {
            itemStack = BLib.getApi().getItemStackSerialize().deserialize(item);
        }
        return itemStack.clone();
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
            if (sb.indexOf("{expires}") != -1) {
                sb.replace(sb.indexOf("{expires}"), sb.indexOf("{expires}") + "{expires}".length(), TimeUtil.getFormat(removalDate));
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

    public HashSet<String> getTags() {
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

    public List<String> getSellFor() {
        return sellFor;
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
                ", uuid=" + uuid +
                ", material=" + material +
                ", amount=" + amount +
                ", priceForOne=" + priceForOne +
                ", sellFor=" + sellFor +
                ", itemStack=" + itemStack +
                '}';
    }
}
