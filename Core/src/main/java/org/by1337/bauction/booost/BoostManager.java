package org.by1337.bauction.booost;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.by1337.api.configuration.YamlContext;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.MemoryUser;


import java.util.ArrayList;
import java.util.List;

public class BoostManager {
    private List<Boost> boosts;

    public BoostManager(YamlContext context) {
        boosts = context.getList("boosts", Boost.class);
    }

    public MemoryUser userUpdate(MemoryUser user) {
        Player player = Bukkit.getPlayer(user.getUuid());
        if (player == null) return user;
        int slots = 0;
        long sellTime = 0L;
        for (Boost boost : boosts) {
            if (player.hasPermission(boost.getPermission())) {
                slots += boost.getExternalSlots();
                sellTime += boost.getExternalSellTime();
            }
        }
        int finalSlots = slots;
        long finalSellTime = sellTime;

        user.setExternalSlots(finalSlots);
        user.setExternalSellTime(finalSellTime);
        return user;
    }

}
