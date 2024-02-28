package org.by1337.bauction.hook.impl;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.by1337.bauction.Main;
import org.by1337.bauction.hook.EconomyHook;
import org.by1337.blib.configuration.YamlContext;

import java.util.UUID;

public class CustomEconomy implements EconomyHook {
    private final String balancePlaceholder;
    private final String giveCommand;
    private final String takeCommand;

    public CustomEconomy(YamlContext context) {
        balancePlaceholder = context.getAsString("balance");
        giveCommand = context.getAsString("give-command");
        takeCommand = context.getAsString("take-command");
    }

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
        return Double.parseDouble(Main.getMessage().setPlaceholders(offlinePlayer, balancePlaceholder));
    }

    @Override
    public void withdrawPlayer(OfflinePlayer offlinePlayer, double count) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Main.getMessage().setPlaceholders(offlinePlayer, takeCommand).replace("{amount}",  String.valueOf(count)));
    }

    @Override
    public void depositPlayer(OfflinePlayer offlinePlayer, double count) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Main.getMessage().setPlaceholders(offlinePlayer, giveCommand).replace("{amount}",  String.valueOf(count)));
    }
}
