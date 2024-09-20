package org.by1337.bauction.common.db.type;

import org.by1337.bauction.common.io.codec.Codec;
import org.by1337.bauction.common.io.codec.UnsoldItemCodec;
import org.by1337.blib.nbt.NBT;
import org.by1337.blib.nbt.impl.CompoundTag;

import java.util.Objects;
import java.util.UUID;

public class UnsoldItem {
    public static final Codec<UnsoldItem> CODEC = new UnsoldItemCodec();
    public final NBT item;
    public final long expired;
    public final UUID sellerUuid;
    public final long id;
    public final long deleteVia;
    public final UUID server;
    public final CompoundTag extra;

    public UnsoldItem(NBT item, long expired, UUID sellerUuid, long id, long deleteVia, UUID server, CompoundTag extra) {
        this.item = item;
        this.expired = expired;
        this.sellerUuid = sellerUuid;
        this.id = id;
        this.deleteVia = deleteVia;
        this.server = server;
        this.extra = extra;
    }

    private UnsoldItem(Builder builder) {
        this.item = builder.item;
        this.expired = builder.expired;
        this.sellerUuid = builder.sellerUuid;
        this.id = builder.id;
        this.deleteVia = builder.deleteVia;
        this.extra = builder.extra;
        this.server = builder.server;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static Builder builder(UnsoldItem unsoldItem) {
        return new Builder(unsoldItem);
    }

    public UnsoldItem setItem(NBT item) {
        return builder(this).item(item).build();
    }

    public UnsoldItem setExpired(long expired) {
        return builder(this).expired(expired).build();
    }

    public UnsoldItem setSellerUuid(UUID sellerUuid) {
        return builder(this).sellerUuid(sellerUuid).build();
    }

    public UnsoldItem setId(long id) {
        return builder(this).id(id).build();
    }

    public UnsoldItem setDeleteVia(long deleteVia) {
        return builder(this).deleteVia(deleteVia).build();
    }

    public UnsoldItem setExtra(CompoundTag extra) {
        return builder(this).extra(extra).build();
    }

    public UnsoldItem setServer(UUID server) {
        return builder(this).server(server).build();
    }

    public NBT getItem() {
        return item;
    }

    public long getExpired() {
        return expired;
    }

    public UUID getSellerUuid() {
        return sellerUuid;
    }

    public long getId() {
        return id;
    }

    public long getDeleteVia() {
        return deleteVia;
    }

    public CompoundTag getExtra() {
        return extra;
    }

    public UUID getServer() {
        return server;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnsoldItem that = (UnsoldItem) o;
        return expired == that.expired && id == that.id && deleteVia == that.deleteVia && Objects.equals(item, that.item) && Objects.equals(sellerUuid, that.sellerUuid) && Objects.equals(server, that.server) && Objects.equals(extra, that.extra);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, expired, sellerUuid, id, deleteVia, server, extra);
    }

    public static class Builder {

        private NBT item;
        private long expired;
        private UUID sellerUuid;
        private long id;
        private long deleteVia;
        private CompoundTag extra;
        private UUID server;

        public Builder() {
        }

        public Builder(NBT item, long expired, UUID sellerUuid, long id, long deleteVia, CompoundTag extra, UUID server) {
            this.item = item;
            this.expired = expired;
            this.sellerUuid = sellerUuid;
            this.id = id;
            this.deleteVia = deleteVia;
            this.extra = extra;
            this.server = server;
        }

        public Builder(UnsoldItem unsoldItem) {
            this.item = unsoldItem.item;
            this.expired = unsoldItem.expired;
            this.sellerUuid = unsoldItem.sellerUuid;
            this.id = unsoldItem.id;
            this.deleteVia = unsoldItem.deleteVia;
            this.extra = unsoldItem.extra;
            this.server = unsoldItem.server;
        }

        public Builder item(NBT item) {
            this.item = item;
            return Builder.this;
        }

        public Builder expired(long expired) {
            this.expired = expired;
            return Builder.this;
        }

        public Builder sellerUuid(UUID sellerUuid) {
            this.sellerUuid = sellerUuid;
            return Builder.this;
        }

        public Builder id(long id) {
            this.id = id;
            return Builder.this;
        }

        public Builder deleteVia(long deleteVia) {
            this.deleteVia = deleteVia;
            return Builder.this;
        }

        public Builder extra(CompoundTag extra) {
            this.extra = extra;
            return Builder.this;
        }
        public Builder server(UUID server) {
            this.server = server;
            return Builder.this;
        }

        public UnsoldItem build() {
            if (this.item == null) {
                throw new NullPointerException("The property \"item\" is null. "
                                               + "Please set the value by \"item()\". "
                                               + "The properties \"item\", \"server\", \"sellerUuid\" and \"extra\" are required.");
            }
            if (this.sellerUuid == null) {
                throw new NullPointerException("The property \"sellerUuid\" is null. "
                                               + "Please set the value by \"sellerUuid()\". "
                                               + "The properties \"item\", \"server\", \"sellerUuid\" and \"extra\" are required.");
            }
            if (this.extra == null) {
                throw new NullPointerException("The property \"extra\" is null. "
                                               + "Please set the value by \"extra()\". "
                                               + "The properties \"item\", \"server\", \"sellerUuid\" and \"extra\" are required.");
            }
            if (this.server == null) {
                throw new NullPointerException("The property \"server\" is null. "
                                               + "Please set the value by \"server()\". "
                                               + "The properties \"item\", \"server\", \"sellerUuid\" and \"extra\" are required.");
            }

            return new UnsoldItem(this);
        }
    }
}
