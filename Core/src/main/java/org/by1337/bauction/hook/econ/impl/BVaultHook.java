package org.by1337.bauction.hook.econ.impl;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.by1337.bauction.hook.econ.EconomyHook;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.bvault.api.BEconomy;

import java.util.Objects;
import java.util.UUID;

public class BVaultHook implements EconomyHook {
    private final BEconomy economy;
    private final String bank;

    public BVaultHook(YamlContext config) {
        RegisteredServiceProvider<BEconomy> rsp = Bukkit.getServer().getServicesManager().getRegistration(BEconomy.class);
        economy = Objects.requireNonNull(rsp, "Economy provider not found!").getProvider();
        bank = config.getAsString("BVault-setting.current-bank");
    }

    @Override
    public double getBalance(UUID uuid) {
        return economy.getBalance(bank, uuid).join();
    }

    @Override
    public void withdrawPlayer(UUID uuid, double count) {
        economy.withdraw(bank, uuid, count);
    }

    @Override
    public void depositPlayer(UUID uuid, double count) {
        economy.deposit(bank, uuid, count);
    }
}
