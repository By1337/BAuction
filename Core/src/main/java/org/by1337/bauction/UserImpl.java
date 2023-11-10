package org.by1337.bauction;

import org.by1337.api.chat.Placeholderable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserImpl implements User {
    private final String nickName;
    private final UUID uuid;
    private List<UnsoldItem> unsoldItemImpls = new ArrayList<>();
    private transient int externalSlots = 0;
    private transient long externalSellTime = 0L;
    private List<UUID> itemForSale = new ArrayList<>();
    private int dealCount;
    private double dealSum;


    public UserImpl(@NotNull String nickName, @NotNull UUID uuid) {
        this.nickName = nickName;
        this.uuid = uuid;
    }

    public boolean hasItemForSale(UUID uuid){
        return itemForSale.contains(uuid);
    }


    public @NotNull String getNickName() {
        return nickName;
    }

    public @NotNull UUID getUuid() {
        return uuid;
    }

    public List<UnsoldItem> getUnsoldItems() {
        return unsoldItemImpls;
    }

    public int getExternalSlots() {
        return externalSlots;
    }

    public long getExternalSellTime() {
        return externalSellTime;
    }

    public List<UUID> getItemForSale() {
        return itemForSale;
    }

    public boolean removeUnsoldItem(UnsoldItem item){
        return unsoldItemImpls.remove(item);
    }

    public boolean addUnsoldItem(UnsoldItem item){
        return unsoldItemImpls.add(item);
    }

    public boolean removeSellItem(UUID uuid) {
        return itemForSale.remove(uuid);
    }

    public boolean addSellItem(UUID uuid) {
        return itemForSale.add(uuid);
    }

    public int getDealCount() {
        return dealCount;
    }

    public double getDealSum() {
        return dealSum;
    }

    public void addDealCount(int count) {
        dealCount += count;
    }

    public void addDealSum(double count) {
        dealSum += count;
    }

    public void takeDealCount(int count) {
        dealCount -= count;
    }

    public void takeDealSum(double count) {
        dealSum -= count;
    }

    public void setDealCount(int dealCount) {
        this.dealCount = dealCount;
    }

    public void setDealSum(double dealSum) {
        this.dealSum = dealSum;
    }

    public void setExternalSlots(int externalSlots) {
        this.externalSlots = externalSlots;
    }

    public void setExternalSellTime(long externalSellTime) {
        this.externalSellTime = externalSellTime;
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
                sb.replace(sb.indexOf("{not_sold_item_count}"), sb.indexOf("{not_sold_item_count}") + "{not_sold_item_count}".length(), String.valueOf(unsoldItemImpls.size()));
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
}
