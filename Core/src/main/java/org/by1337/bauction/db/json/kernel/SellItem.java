package org.by1337.bauction.db.json.kernel;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.api.BLib;
import org.by1337.bauction.db.MemorySellItem;
import org.by1337.bauction.util.TagUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

class SellItem {
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

    SellItem(String item, String sellerName, UUID sellerUuid,
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

    SellItem(@NotNull String item, @NotNull String sellerName, @NotNull UUID sellerUuid, double price, boolean saleByThePiece, @NotNull Set<String> tags, long saleDuration, @NotNull Material material, int amount) {
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

    SellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration) {
        this(seller, itemStack, price, saleDuration, true);
    }

    SellItem(@NotNull Player seller, @NotNull ItemStack itemStack, double price, long saleDuration, boolean saleByThePiece) {
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

    static SellItem parse(MemorySellItem item) {
        return new SellItem(
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

    MemorySellItem toMemorySellItem() {
        return MemorySellItem.builder()
                .sellerName(sellerName)
                .sellerUuid(sellerUuid)
                .price(price)
                .saleByThePiece(saleByThePiece)
                .tags(Collections.unmodifiableSet(tags))
                .timeListedForSale(timeListedForSale)
                .removalDate(removalDate)
                .uuid(uuid)
                .material(material)
                .amount(amount)
                .priceForOne(priceForOne)
                .sellFor(Collections.unmodifiableSet(sellFor))
                .itemStack(BLib.getApi().getItemStackSerialize().deserialize(item))
                .build();
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
                '}';
    }
}
