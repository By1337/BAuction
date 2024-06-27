package org.by1337.bauction.auto.buy;

import org.bukkit.inventory.ItemStack;
import org.by1337.blib.BLib;
import org.by1337.bauction.db.kernel.SellItem;
import org.by1337.bauction.api.serialize.SerializableToByteArray;
import org.by1337.bauction.serialize.SerializeUtils;
import org.by1337.bauction.util.auction.TagUtil;

import java.io.*;
import java.util.Collection;
import java.util.HashSet;

public class BuyingItem implements SerializableToByteArray {
    private final String item;
    private double maxPrice;
    private int buyChance;
    private boolean buyOnlyEquals;
    private final HashSet<String> tags;
    private transient ItemStack itemStack;
    private final int page;
    private final int slot;

    public BuyingItem(double maxPrice, int buyChance, ItemStack itemStack, int page, int slot) {
        item = BLib.getApi().getItemStackSerialize().serialize(itemStack);
        this.maxPrice = maxPrice;
        this.buyChance = buyChance;
        this.itemStack = itemStack;
        tags = TagUtil.getTags(itemStack);
        this.page = page;
        this.slot = slot;
    }

    public BuyingItem(String item, double maxPrice, int buyChance, boolean buyOnlyEquals, HashSet<String> tags, int page, int slot) {
        this.item = item;
        this.maxPrice = maxPrice;
        this.buyChance = buyChance;
        this.buyOnlyEquals = buyOnlyEquals;
        this.tags = tags;
        this.page = page;
        this.slot = slot;
    }

    public ItemStack getItemStack() {
        if (itemStack == null) {
            itemStack = BLib.getApi().getItemStackSerialize().deserialize(item);
        }
        return itemStack.clone();
    }

    public boolean canBuy(SellItem sellItem) {
        if ((sellItem.getPrice() / sellItem.getAmount()) > maxPrice) return false;
        TagsEqualsState state = checkTags(sellItem.getTags());
        if (state == TagsEqualsState.NOT_EQUALS) return false;
        return !buyOnlyEquals || state == TagsEqualsState.EQUALS;
    }

    public TagsEqualsState checkTags(Collection<String> tags) {
        for (String tag : this.tags) {
            if (!tags.contains(tag)) return TagsEqualsState.NOT_EQUALS;
        }
        return this.tags.size() == tags.size() ? TagsEqualsState.EQUALS : TagsEqualsState.HAS_ADDITIONAL_TAGS;
    }


    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public void setBuyChance(int buyChance) {
        this.buyChance = buyChance;
    }

    public void setBuyOnlyEquals(boolean buyOnlyEquals) {
        this.buyOnlyEquals = buyOnlyEquals;
    }

    public String getItem() {
        return item;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public int getBuyChance() {
        return buyChance;
    }

    public boolean isBuyOnlyEquals() {
        return buyOnlyEquals;
    }

    public HashSet<String> getTags() {
        return tags;
    }

    public int getPage() {
        return page;
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public byte[] getBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             DataOutputStream data = new DataOutputStream(out)) {
            data.writeUTF(item);
            data.writeDouble(maxPrice);
            data.writeInt(buyChance);
            data.writeBoolean(buyOnlyEquals);
            SerializeUtils.writeCollectionToStream(data, tags);
            data.writeInt(page);
            data.writeInt(slot);
            data.flush();
            return out.toByteArray();
        }
    }

    public static BuyingItem fromBytes(byte[] arr) throws IOException {
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(arr))) {
            final String item = in.readUTF();
            double maxPrice = in.readDouble();
            int buyChance = in.readInt();
            boolean buyOnlyEquals = in.readBoolean();
            HashSet<String> tags = new HashSet<>(SerializeUtils.readCollectionFromStream(in));
            int page = in.readInt();
            int slot = in.readInt();
            return new BuyingItem(item, maxPrice, buyChance, buyOnlyEquals, tags, page, slot);
        }
    }

    public enum TagsEqualsState {
        EQUALS,
        NOT_EQUALS,
        HAS_ADDITIONAL_TAGS
    }
}
