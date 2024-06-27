package org.by1337.bauction.boost;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.bauction.db.kernel.User;
import org.jetbrains.annotations.NotNull;


import java.util.List;

public class BoostManager {
    private final List<Boost> boosts;

    public BoostManager(YamlContext context) {
        boosts = context.getList("boosts", Boost.class);
    }

    public User userUpdate(@NotNull User user) {
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
        user.setExternalSlots(slots);
        user.setExternalSellTime(sellTime);
        return user;
    }

}
