package org.by1337.bauction.db.kernel;

import lombok.Builder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.BLib;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.NumberUtil;
import org.by1337.bauction.util.TagUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Builder
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
    final Set<String> sellFor = new HashSet<>();
    private transient ItemStack itemStack;

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
    }

    static CSellItem parse(CSellItem item) {
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

}
