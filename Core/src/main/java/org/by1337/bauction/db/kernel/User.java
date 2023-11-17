package org.by1337.bauction.db.kernel;

import org.by1337.bauction.db.MemoryUser;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

class User {
    final String nickName;
    final UUID uuid;

    List<UUID> unsoldItems = new ArrayList<>();

    List<UUID> itemForSale = new ArrayList<>();
    int dealCount;
    double dealSum;


    User(@NotNull String nickName, @NotNull UUID uuid) {
        this.nickName = nickName;
        this.uuid = uuid;
    }


    MemoryUser toMemoryUser(){
        return MemoryUser.builder()
                .nickName(nickName)
                .uuid(uuid)
                .unsoldItems(Collections.unmodifiableList(unsoldItems))
                .itemForSale(Collections.unmodifiableList(itemForSale))
                .dealCount(dealCount)
                .dealSum(dealSum)
                .build();
    }

    @Override
    public String toString() {
        return "User{" +
                "nickName='" + nickName + '\'' +
                ", uuid=" + uuid +
                ", unsoldItemImpls=" + unsoldItems +
//                ", externalSlots=" + externalSlots +
//                ", externalSellTime=" + externalSellTime +
                ", itemForSale=" + itemForSale +
                ", dealCount=" + dealCount +
                ", dealSum=" + dealSum +
                '}';
    }


}
