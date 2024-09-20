package org.by1337.bauction.db.kernel;

import org.by1337.bauction.Main;
import org.by1337.bauction.common.db.type.User;
import org.by1337.blib.chat.placeholder.Placeholder;
import org.by1337.blib.nbt.impl.CompoundTag;

import java.util.Objects;
import java.util.UUID;

public class PluginUser extends Placeholder {

    private User source;

    public PluginUser(User source) {
        this.source = source;
        init();
    }

    public synchronized void setSource(User source) {
        this.source = source;
        init();
    }

    public User getSource() {
        return source;
    }

    public int getMaxSellItems() {
        return Main.getCfg().getMaxSlots() + getExternalSlots();
    }

    private void init() {
        registerPlaceholder("{user_uuid}", this::getUuid);
        registerPlaceholder("{deal_sum}", this::getDealSum);
        registerPlaceholder("{nick_name}", this::getNickName);
        registerPlaceholder("{deal_count}", this::getDealCount);
        registerPlaceholder("{selling_item_count}", () -> Main.getStorage().getSellItemsCountByUser(getUuid()));
        registerPlaceholder("{not_sold_item_count}", () -> Main.getStorage().getUnsoldItemsCountByUser(getUuid()));
        registerPlaceholder("{external_slots}", this::getExternalSlots);
        registerPlaceholder("{slots_count}", this::getMaxSellItems);
        registerPlaceholder("{external_sell_time}", () -> Main.getTimeUtil().getFormat(getExternalSellTime(), false));
    }

    public String getNickName() {
        return source.getNickName();
    }

    public UUID getUuid() {
        return source.getUuid();
    }

    public synchronized int getDealCount() {
        return source.getDealCount();
    }

    public synchronized double getDealSum() {
        return source.getDealSum();
    }

    public synchronized int getExternalSlots() {
        return source.getExternalSlots();
    }

    public synchronized long getExternalSellTime() {
        return source.getExternalSellTime();
    }

    public synchronized CompoundTag getExtra() {
        return source.getExtra();
    }

    @Override
    public String toString() {
        return "PluginUser{" +
               "source=" + source +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PluginUser that = (PluginUser) o;
        return Objects.equals(source, that.source);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(source);
    }
}
