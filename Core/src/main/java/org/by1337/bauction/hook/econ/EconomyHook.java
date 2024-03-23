package org.by1337.bauction.hook.econ;

import org.bukkit.OfflinePlayer;

import java.util.UUID;

public interface EconomyHook {
    double getBalance(UUID uuid);

    void withdrawPlayer(UUID uuid, double count);

    void depositPlayer(UUID uuid, double count);

    default double getBalance(OfflinePlayer offlinePlayer) {
        return getBalance(offlinePlayer.getUniqueId());
    }

    default void withdrawPlayer(OfflinePlayer offlinePlayer, double count) {
        withdrawPlayer(offlinePlayer.getUniqueId(), count);
    }

    default void depositPlayer(OfflinePlayer offlinePlayer, double count) {
        depositPlayer(offlinePlayer.getUniqueId(), count);
    }
}
