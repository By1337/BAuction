package org.by1337.bauction;

import org.by1337.bauction.db.json.User;

import java.util.List;
import java.util.UUID;

public class EditableUserImpl implements EditableUser {
    private User handle;

    public EditableUserImpl(User handle) {
        this.handle = handle;
    }

    @Override
    public List<UnsoldItem> getUnsoldItems() {
        return handle.getUnsoldItems();
    }

    @Override
    public List<UUID> getItemForSale() {
        return handle.getItemForSale();
    }

    @Override
    public boolean removeUnsoldItem(UnsoldItem item) {
        return handle.removeUnsoldItem(item);
    }

    @Override
    public boolean addUnsoldItem(UnsoldItem item) {
        return handle.addUnsoldItem(item);
    }

    @Override
    public boolean removeSellItem(UUID uuid) {
        return handle.removeSellItem(uuid);
    }

    @Override
    public boolean addSellItem(UUID uuid) {
        return handle.addSellItem(uuid);
    }

    @Override
    public void addDealCount(int count) {
        handle.addDealCount(count);
    }

    @Override
    public void addDealSum(double count) {
        handle.addDealSum(count);
    }

    @Override
    public void takeDealCount(int count) {
        handle.takeDealCount(count);
    }

    @Override
    public void takeDealSum(double count) {
        handle.takeDealSum(count);
    }

    @Override
    public String getNickName() {
        return handle.getNickName();
    }

    @Override
    public UUID getUuid() {
        return handle.getUuid();
    }

    @Override
    public int getExternalSlots() {
        return handle.getExternalSlots();
    }

    @Override
    public long getExternalSellTime() {
        return handle.getExternalSellTime();
    }

    @Override
    public int getDealCount() {
        return handle.getDealCount();
    }

    @Override
    public double getDealSum() {
        return handle.getDealSum();
    }

    @Override
    public String replace(String s) {
        return handle.replace(s);
    }

    @Override
    public void setExternalSlots(int externalSlots) {
        handle.setExternalSlots(externalSlots);
    }

    @Override
    public void setExternalSellTime(long externalSellTime) {
        handle.setExternalSellTime(externalSellTime);
    }

    public void setHandle(User handle) {
        this.handle = handle;
    }
}
