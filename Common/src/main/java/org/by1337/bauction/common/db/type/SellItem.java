package org.by1337.bauction.common.db.type;

import org.by1337.bauction.common.io.codec.Codec;
import org.by1337.bauction.common.io.codec.SellItemCodec;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.impl.CompoundTag;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class SellItem {
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
    public final int material;
    public final int amount;
    public final transient double priceForOne;
    public final String server;
    public final CompoundTag extra;

    public SellItem(NBT item, String sellerName, UUID sellerUuid, double price, boolean saleByThePiece, Set<String> tags, long timeListedForSale, long removalDate, long id, int material, int amount, String server, CompoundTag extra) {
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
        this.priceForOne = price / amount;
        this.server = server;
        this.extra = extra;
    }

    private SellItem(Builder builder) {
        this.item = builder.item;
        this.sellerName = builder.sellerName;
        this.sellerUuid = builder.sellerUuid;
        this.price = builder.price;
        this.saleByThePiece = builder.saleByThePiece;
        this.tags = builder.tags;
        this.timeListedForSale = builder.timeListedForSale;
        this.removalDate = builder.removalDate;
        this.id = builder.id;
        this.material = builder.material;
        this.amount = builder.amount;
        this.server = builder.server;
        this.extra = builder.extra;
        this.priceForOne = price / amount;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(SellItem sellItem) {
        return new Builder(sellItem);
    }

    public SellItem setItem(NBT item) {
        return SellItem.builder(this).item(item).build();
    }

    public SellItem setSellerName(String sellerName) {
        return SellItem.builder(this).sellerName(sellerName).build();
    }

    public SellItem setSellerUuid(UUID sellerUuid) {
        return SellItem.builder(this).sellerUuid(sellerUuid).build();
    }

    public SellItem setPrice(double price) {
        return SellItem.builder(this).price(price).build();
    }

    public SellItem setSaleByThePiece(boolean saleByThePiece) {
        return SellItem.builder(this).saleByThePiece(saleByThePiece).build();
    }

    public SellItem setTags(Set<String> tags) {
        return SellItem.builder(this).tags(tags).build();
    }

    public SellItem setTimeListedForSale(long timeListedForSale) {
        return SellItem.builder(this).timeListedForSale(timeListedForSale).build();
    }

    public SellItem setRemovalDate(long removalDate) {
        return SellItem.builder(this).removalDate(removalDate).build();
    }

    public SellItem setId(long id) {
        return SellItem.builder(this).id(id).build();
    }

    public SellItem setMaterial(int material) {
        return SellItem.builder(this).material(material).build();
    }

    public SellItem setAmount(int amount) {
        return SellItem.builder(this).amount(amount).build();
    }

    public SellItem setServer(String server) {
        return SellItem.builder(this).server(server).build();
    }

    public SellItem setExtra(CompoundTag extra) {
        return SellItem.builder(this).extra(extra).build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SellItem sellItem = (SellItem) o;
        return Double.compare(price, sellItem.price) == 0 && saleByThePiece == sellItem.saleByThePiece && timeListedForSale == sellItem.timeListedForSale && removalDate == sellItem.removalDate && id == sellItem.id && material == sellItem.material && amount == sellItem.amount && Double.compare(priceForOne, sellItem.priceForOne) == 0 && Objects.equals(item, sellItem.item) && Objects.equals(sellerName, sellItem.sellerName) && Objects.equals(sellerUuid, sellItem.sellerUuid) && Objects.equals(tags, sellItem.tags) && Objects.equals(server, sellItem.server) && Objects.equals(extra, sellItem.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, sellerName, sellerUuid, price, saleByThePiece, tags, timeListedForSale, removalDate, id, material, amount, priceForOne, server, extra);
    }

    public static class Builder {
        private NBT item;
        private String sellerName;
        private UUID sellerUuid;
        private double price;
        private boolean saleByThePiece;
        private Set<String> tags;
        private long timeListedForSale;
        private long removalDate;
        private long id;
        private int material;
        private int amount;
        private String server;
        private CompoundTag extra;

        public Builder() {
        }

        public Builder(SellItem sellItem) {
            this.item = sellItem.item;
            this.sellerName = sellItem.sellerName;
            this.sellerUuid = sellItem.sellerUuid;
            this.price = sellItem.price;
            this.saleByThePiece = sellItem.saleByThePiece;
            this.tags = sellItem.tags;
            this.timeListedForSale = sellItem.timeListedForSale;
            this.removalDate = sellItem.removalDate;
            this.id = sellItem.id;
            this.material = sellItem.material;
            this.amount = sellItem.amount;
            this.server = sellItem.server;
            this.extra = sellItem.extra;
        }

        Builder(NBT item, String sellerName, UUID sellerUuid, double price, boolean saleByThePiece, Set<String> tags, long timeListedForSale, long removalDate, long id, int material, int amount, String server, CompoundTag extra) {
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
            this.server = server;
            this.extra = extra;
        }

        public Builder item(NBT item) {
            this.item = item;
            return Builder.this;
        }

        public Builder sellerName(String sellerName) {
            this.sellerName = sellerName;
            return Builder.this;
        }

        public Builder sellerUuid(UUID sellerUuid) {
            this.sellerUuid = sellerUuid;
            return Builder.this;
        }

        public Builder price(double price) {
            this.price = price;
            return Builder.this;
        }

        public Builder saleByThePiece(boolean saleByThePiece) {
            this.saleByThePiece = saleByThePiece;
            return Builder.this;
        }

        public Builder tags(Set<String> tags) {
            this.tags = tags;
            return Builder.this;
        }

        public Builder timeListedForSale(long timeListedForSale) {
            this.timeListedForSale = timeListedForSale;
            return Builder.this;
        }

        public Builder removalDate(long removalDate) {
            this.removalDate = removalDate;
            return Builder.this;
        }

        public Builder id(long id) {
            this.id = id;
            return Builder.this;
        }

        public Builder material(int material) {
            this.material = material;
            return Builder.this;
        }

        public Builder amount(int amount) {
            this.amount = amount;
            return Builder.this;
        }

        public Builder server(String server) {
            this.server = server;
            return Builder.this;
        }

        public Builder extra(CompoundTag extra) {
            this.extra = extra;
            return Builder.this;
        }

        public SellItem build() {
            if (this.item == null) {
                throw new NullPointerException("The property \"item\" is null. "
                                               + "Please set the value by \"item()\". "
                                               + "The properties \"item\", \"sellerName\", \"sellerUuid\", \"tags\", \"server\" and \"extra\" are required.");
            }
            if (this.sellerName == null) {
                throw new NullPointerException("The property \"sellerName\" is null. "
                                               + "Please set the value by \"sellerName()\". "
                                               + "The properties \"item\", \"sellerName\", \"sellerUuid\", \"tags\", \"server\" and \"extra\" are required.");
            }
            if (this.sellerUuid == null) {
                throw new NullPointerException("The property \"sellerUuid\" is null. "
                                               + "Please set the value by \"sellerUuid()\". "
                                               + "The properties \"item\", \"sellerName\", \"sellerUuid\", \"tags\", \"server\" and \"extra\" are required.");
            }
            if (this.tags == null) {
                throw new NullPointerException("The property \"tags\" is null. "
                                               + "Please set the value by \"tags()\". "
                                               + "The properties \"item\", \"sellerName\", \"sellerUuid\", \"tags\", \"server\" and \"extra\" are required.");
            }
            if (this.server == null) {
                throw new NullPointerException("The property \"server\" is null. "
                                               + "Please set the value by \"server()\". "
                                               + "The properties \"item\", \"sellerName\", \"sellerUuid\", \"tags\", \"server\" and \"extra\" are required.");
            }
            if (this.extra == null) {
                throw new NullPointerException("The property \"extra\" is null. "
                                               + "Please set the value by \"extra()\". "
                                               + "The properties \"item\", \"sellerName\", \"sellerUuid\", \"tags\", \"server\" and \"extra\" are required.");
            }
            return new SellItem(this);
        }
    }


}
