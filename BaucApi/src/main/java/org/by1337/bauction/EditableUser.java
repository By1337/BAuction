package org.by1337.bauction;

import org.by1337.api.chat.Placeholderable;

import java.util.List;
import java.util.UUID;

public interface EditableUser
        extends Placeholderable {
    List<UnsoldItem> getUnsoldItems();

    List<UUID> getItemForSale();

    boolean removeUnsoldItem(UnsoldItem item);

    boolean addUnsoldItem(UnsoldItem item);

    boolean removeSellItem(UUID uuid);

    boolean addSellItem(UUID uuid);

    void addDealCount(int count);

    void addDealSum(int count);

    void takeDealCount(int count);

    void takeDealSum(int count);

    String getNickName();

    UUID getUuid();

    int getExternalSlots();

    long getExternalSellTime();

    int getDealCount();

    int getDealSum();

     void setExternalSlots(int externalSlots);

     void setExternalSellTime(long externalSellTime);
}
