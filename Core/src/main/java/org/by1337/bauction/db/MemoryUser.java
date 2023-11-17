package org.by1337.bauction.db;

import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;

import java.util.List;
import java.util.UUID;

public class MemoryUser implements Placeholderable {
    private final String nickName;
    private final UUID uuid;
    private final List<UUID> unsoldItems;
    private final List<UUID> itemForSale;
    private final int dealCount;
    private final double dealSum;
    private volatile int externalSlots = 0;
    private volatile long externalSellTime = 0L;

    public MemoryUser(String nickName, UUID uuid, List<UUID> unsoldItems, List<UUID> itemForSale, int dealCount, double dealSum) {
        this.nickName = nickName;
        this.uuid = uuid;
        this.unsoldItems = unsoldItems;
        this.itemForSale = itemForSale;
        this.dealCount = dealCount;
        this.dealSum = dealSum;
    }


    public static MemoryUserBuilder builder() {
        return new MemoryUserBuilder();
    }

    public void setExternalSlots(int externalSlots) {
        this.externalSlots = externalSlots;
    }

    public void setExternalSellTime(long externalSellTime) {
        this.externalSellTime = externalSellTime;
    }

    public String getNickName() {
        return nickName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<UUID> getUnsoldItems() {
        return unsoldItems;
    }

    public List<UUID> getItemForSale() {
        return itemForSale;
    }

    public int getDealCount() {
        return dealCount;
    }

    public double getDealSum() {
        return dealSum;
    }

    public int getExternalSlots() {
        return externalSlots;
    }

    public long getExternalSellTime() {
        return externalSellTime;
    }

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (true) {
            if (sb.indexOf("{deal_sum}") != -1) {
                sb.replace(sb.indexOf("{deal_sum}"), sb.indexOf("{deal_sum}") + "{deal_sum}".length(), String.valueOf(dealSum));
                continue;
            }
            if (sb.indexOf("{deal_count}") != -1) {
                sb.replace(sb.indexOf("{deal_count}"), sb.indexOf("{deal_count}") + "{deal_count}".length(), String.valueOf(dealCount));
                continue;
            }
            if (sb.indexOf("{selling_item_count}") != -1) {
                sb.replace(sb.indexOf("{selling_item_count}"), sb.indexOf("{selling_item_count}") + "{selling_item_count}".length(), String.valueOf(itemForSale.size()));
                continue;
            }
            if (sb.indexOf("{not_sold_item_count}") != -1) {
                sb.replace(sb.indexOf("{not_sold_item_count}"), sb.indexOf("{not_sold_item_count}") + "{not_sold_item_count}".length(), String.valueOf(unsoldItems.size()));
                continue;
            }
            if (sb.indexOf("{external_slots}") != -1) {
                sb.replace(sb.indexOf("{external_slots}"), sb.indexOf("{external_slots}") + "{external_slots}".length(), String.valueOf(externalSlots));
                continue;
            }
            if (sb.indexOf("{slots_count}") != -1) {
                sb.replace(sb.indexOf("{slots_count}"), sb.indexOf("{slots_count}") + "{slots_count}".length(), String.valueOf(Main.getCfg().getMaxSlots() + externalSlots));
                continue;
            }
            break;
        }
        return sb.toString();
    }

    public static class MemoryUserBuilder {
        private String nickName;
        private UUID uuid;
        private List<UUID> unsoldItems;
        private List<UUID> itemForSale;
        private int dealCount;
        private double dealSum;

        MemoryUserBuilder() {
        }

        public MemoryUserBuilder nickName(String nickName) {
            this.nickName = nickName;
            return this;
        }

        public MemoryUserBuilder uuid(UUID uuid) {
            this.uuid = uuid;
            return this;
        }

        public MemoryUserBuilder unsoldItems(List<UUID> unsoldItems) {
            this.unsoldItems = unsoldItems;
            return this;
        }

        public MemoryUserBuilder itemForSale(List<UUID> itemForSale) {
            this.itemForSale = itemForSale;
            return this;
        }

        public MemoryUserBuilder dealCount(int dealCount) {
            this.dealCount = dealCount;
            return this;
        }

        public MemoryUserBuilder dealSum(double dealSum) {
            this.dealSum = dealSum;
            return this;
        }

        public MemoryUser build() {
            return new MemoryUser(this.nickName, this.uuid, this.unsoldItems, this.itemForSale, this.dealCount, this.dealSum);
        }

    }
}
