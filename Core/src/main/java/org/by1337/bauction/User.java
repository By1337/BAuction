package org.by1337.bauction;

import org.by1337.api.chat.Placeholderable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User implements Placeholderable {
    private final String nickName;
    private final UUID uuid;
    private List<UnsoldItem> unsoldItems = new ArrayList<>();
    private transient int externalSlots = 0;
    private transient long externalSellTime = 0L;
    private List<UUID> itemForSale = new ArrayList<>();

    public User(@NotNull String nickName, @NotNull UUID uuid) {
        this.nickName = nickName;
        this.uuid = uuid;
    }

    public String getNickName() {
        return nickName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<UnsoldItem> getUnsoldItems() {
        return unsoldItems;
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

    @Override
    public String replace(String s) {
        StringBuilder sb = new StringBuilder(s);
        while (true) {
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
            break;
        }
        return sb.toString();
    }
}
