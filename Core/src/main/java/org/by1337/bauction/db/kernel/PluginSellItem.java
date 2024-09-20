package org.by1337.bauction.db.kernel;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.ItemHolder;
import org.by1337.bauction.common.db.type.SellItem;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.util.auction.Category;
import org.by1337.bauction.util.common.NumberUtil;
import org.by1337.blib.BLib;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.nbt.CompressedNBT;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.impl.ByteArrNBT;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class PluginSellItem extends Placeholder implements ItemHolder {

    private SellItem source;

    @Nullable
    private transient ItemStack itemStack;

    public PluginSellItem(SellItem source) {
        this.source = source;
        init();
    }

    void setSource(SellItem source) {
        this.source = source;
        init();
    }

    public SellItem getSource() {
        return source;
    }

    private void init() {
        registerPlaceholder("{seller_uuid}", this::getSellerUuid);
        registerPlaceholder("{seller_name}", this::getSellerName);
        registerPlaceholder("{price}", () -> NumberUtil.format(getPrice()));
        registerPlaceholder("{price_format}", () -> NumberUtil.formatNumberWithThousandsSeparator(getPrice()));
        registerPlaceholder("{sale_by_the_piece}", this::isSaleByThePiece);
        registerPlaceholder("{sale_by_the_piece_format}", () -> isSaleByThePiece() ?
                Lang.getMessage("sale-by-the-piece-format-on") : Lang.getMessage("sale-by-the-piece-format-off"));
        registerPlaceholder("{expires}", () -> Main.getTimeUtil().getFormat(getRemovalDate()));
        registerPlaceholder("{price_for_one}", () -> NumberUtil.format(getPriceForOne()));
        registerPlaceholder("{price_for_one_format}", () -> NumberUtil.formatNumberWithThousandsSeparator(getPriceForOne()));
        registerPlaceholder("{material}", this::getBukkitMaterial);
        registerPlaceholder("{amount}", this::getAmount);
        registerPlaceholder("{id}", this::getId);
        registerPlaceholder("{sale_time}", () -> getTimeListedForSale() / 1000);
        registerPlaceholder("{item_name}", () -> getItemStack().getItemMeta() != null && getItemStack().getItemMeta().hasDisplayName() ?
                getItemStack().getItemMeta().getDisplayName() :
                Lang.getMessage(getBukkitMaterial().name().toLowerCase()));
        registerPlaceholder("{seller_is_online}", () -> Bukkit.getPlayer(getSellerUuid()) != null);
        registerPlaceholder("{seller_is_online_format}", () ->
                (Bukkit.getPlayer(getSellerUuid()) != null) ?
                        Lang.getMessage("online-seller") : Lang.getMessage("offline-seller")
        );
    }

    public ItemStack getItemStack() {
        if (itemStack == null) {
            if (getItem() instanceof ByteArrNBT arrNBT) {
                CompoundTag tag = (CompoundTag) new CompressedNBT(arrNBT.getValue()).decompress();
                itemStack = BLib.getApi().getParseCompoundTag().create(tag);
            } else {
                itemStack = BLib.getApi().getParseCompoundTag().create((CompoundTag) getItem());
            }
        }
        return itemStack.clone();
    }

    public boolean hasAllTags(Category category) {
        return getTags().containsAll(category.tags());
    }

    public Material getBukkitMaterial() {
        return Material.values()[source.getMaterial()];
    }

    public static NBT serializeItemStack(ItemStack itemStack) {
        CompoundTag tag = BLib.getApi().getParseCompoundTag().copy(itemStack);
        if (tag.getSizeInBytes() > Main.getCfg().getCompressIfMoreThan()) {
            return tag.getAsCompressedNBT();
        } else {
            return tag;
        }
    }

    public NBT getItem() {
        return source.getItem();
    }

    public String getSellerName() {
        return source.getSellerName();
    }

    public UUID getSellerUuid() {
        return source.getSellerUuid();
    }

    public double getPrice() {
        return source.getPrice();
    }

    public boolean isSaleByThePiece() {
        return source.isSaleByThePiece();
    }

    public Set<String> getTags() {
        return source.getTags();
    }

    public long getTimeListedForSale() {
        return source.getTimeListedForSale();
    }

    public long getRemovalDate() {
        return source.getRemovalDate();
    }

    public long getId() {
        return source.getId();
    }

    public int getMaterial() {
        return source.getMaterial();
    }

    public int getAmount() {
        return source.getAmount();
    }

    public double getPriceForOne() {
        return source.getPriceForOne();
    }

    public String getServer() {
        return source.getServer();
    }

    public CompoundTag getExtra() {
        return source.getExtra();
    }

    @Override
    public String toString() {
        return "PluginSellItem{" +
               "source=" + source +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginSellItem that = (PluginSellItem) o;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(source);
    }
}
