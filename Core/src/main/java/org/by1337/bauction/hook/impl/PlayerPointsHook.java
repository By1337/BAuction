package org.by1337.bauction.hook.impl;

import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.by1337.bauction.hook.EconomyHook;

import java.util.UUID;

public class PlayerPointsHook implements EconomyHook {
    private final PlayerPointsAPI ppAPI;

    public PlayerPointsHook() {
        if (!Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")){
            throw new UnsupportedOperationException("PlayerPoints plugin not found!");
        }
        this.ppAPI = PlayerPoints.getInstance().getAPI();
    }

    @Override
    public double getBalance(UUID uuid) {
        return ppAPI.look(uuid);
    }

    @Override
    public void withdrawPlayer(UUID uuid, double count) {
        ppAPI.take(uuid, (int) count);
    }

    @Override
    public void depositPlayer(UUID uuid, double count) {
        ppAPI.give(uuid, (int) count);
    }
}
