package org.by1337.bauction.db.kernel;

import org.by1337.bauction.Main;
import org.by1337.bauction.auc.User;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CUser implements User {
    final String nickName;
    final UUID uuid;
    List<UUID> unsoldItems = new ArrayList<>();
    List<UUID> itemForSale = new ArrayList<>();
    int dealCount;
    double dealSum;
    private int externalSlots = 0;
    private long externalSellTime = 0L;

    public String toSql(String table) {
        return String.format(
                "INSERT INTO %s (uuid, name, unsold_items, item_for_sale, deal_count, deal_sum)" +
                        "VALUES('%s', '%s', '%s', '%s', %s, %s)", table, uuid, nickName, listToString(unsoldItems), listToString(itemForSale), dealCount, dealSum
        );
    }

    public String toSqlUpdate(String table) {
        return String.format(
                "UPDATE %s SET uuid = '%s', name = '%s', unsold_items = '%s', item_for_sale = '%s', deal_count = %s, deal_sum = %s WHERE uuid = '%s';", table, uuid, nickName, listToString(unsoldItems), listToString(itemForSale), dealCount, dealSum, uuid
        );
    }

    private static String listToString(Collection<?> collection) {
        StringBuilder sb = new StringBuilder();
        for (Object o : collection) {
            sb.append(o).append(",");
        }
        if (!sb.isEmpty()) {
            sb.setLength(sb.length() - 1);
        }
        return sb.toString();
    }

    public CUser(String nickName, UUID uuid, List<UUID> unsoldItems, List<UUID> itemForSale, int dealCount, double dealSum) {
        this.nickName = nickName;
        this.uuid = uuid;
        this.unsoldItems = unsoldItems;
        this.itemForSale = itemForSale;
        this.dealCount = dealCount;
        this.dealSum = dealSum;

    }

    public CUser(@NotNull String nickName, @NotNull UUID uuid) {
        this.nickName = nickName;
        this.uuid = uuid;
    }

    public static CUser fromResultSet(ResultSet resultSet) throws SQLException {
        String nickName = resultSet.getString("name");
        UUID uuid = UUID.fromString(resultSet.getString("uuid"));
        List<UUID> unsoldItems = parseUUIDList(resultSet.getString("unsold_items"));
        List<UUID> itemForSale = parseUUIDList(resultSet.getString("item_for_sale"));
        int dealCount = resultSet.getInt("deal_count");
        double dealSum = resultSet.getDouble("deal_sum");

        return new CUser(nickName, uuid, unsoldItems, itemForSale, dealCount, dealSum);
    }

    private static List<UUID> parseUUIDList(String uuidString) {
        List<UUID> uuidList = new ArrayList<>();
        if (!uuidString.isEmpty()) {
            String[] uuidArray = uuidString.split(",");
            for (String uuid : uuidArray) {
                uuidList.add(UUID.fromString(uuid));
            }
        }
        return uuidList;
    }

    public void setExternalSlots(int externalSlots) {
        this.externalSlots = externalSlots;
    }

    public void setExternalSellTime(long externalSellTime) {
        this.externalSellTime = externalSellTime;
    }

    @Override
    public String toString() {
        return "CUser{" +
                "nickName='" + nickName + '\'' +
                ", uuid=" + uuid +
                ", unsoldItemImpls=" + unsoldItems +
                ", externalSlots=" + externalSlots +
                ", externalSellTime=" + externalSellTime +
                ", itemForSale=" + itemForSale +
                ", dealCount=" + dealCount +
                ", dealSum=" + dealSum +
                '}';
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
            if (sb.indexOf("{nick_name}") != -1) {
                sb.replace(sb.indexOf("{nick_name}"), sb.indexOf("{nick_name}") + "{nick_name}".length(), nickName);
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
            if (sb.indexOf("{external_sell_time}") != -1) {
                sb.replace(sb.indexOf("{external_sell_time}"), sb.indexOf("{external_sell_time}") + "{external_sell_time}".length(), Main.getTimeUtil().getFormat(externalSellTime, false));
                continue;
            }
            break;
        }
        return sb.toString();
    }
}
