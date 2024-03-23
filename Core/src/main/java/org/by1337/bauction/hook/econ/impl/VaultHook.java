package org.by1337.bauction.hook.econ.impl;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.by1337.bauction.hook.econ.EconomyHook;

import java.util.Objects;
import java.util.UUID;

public class VaultHook implements EconomyHook {
    private final Economy econ;

    public VaultHook() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        econ = Objects.requireNonNull(rsp, "Economy not found!").getProvider();
    }

    @Override

    public double getBalance(UUID uuid) {
        return getBalance(Bukkit.getOfflinePlayer(uuid));
    }

    @Override
    public void withdrawPlayer(UUID uuid, double count) {
        withdrawPlayer(Bukkit.getOfflinePlayer(uuid), count);
    }

    @Override
    public void depositPlayer(UUID uuid, double count) {
        depositPlayer(Bukkit.getOfflinePlayer(uuid), count);
    }

    @Override
    public double getBalance(OfflinePlayer offlinePlayer) {
        return econ.getBalance(offlinePlayer);
    }

    @Override
    public void withdrawPlayer(OfflinePlayer offlinePlayer, double count) {
        econ.withdrawPlayer(offlinePlayer, count);
    }

    @Override
    public void depositPlayer(OfflinePlayer offlinePlayer, double count) {
        econ.depositPlayer(offlinePlayer, count);
    }
}
