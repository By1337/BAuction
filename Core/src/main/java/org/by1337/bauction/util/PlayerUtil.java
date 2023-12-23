package org.by1337.bauction.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.by1337.bauction.Main;

import java.util.ArrayList;

public class PlayerUtil {

    public static void giveItems(Player player, ItemStack... itemStack) {
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> new ArrayList<>(player.getInventory().addItem(itemStack).values()).forEach(i -> player.getWorld().dropItem(player.getLocation(), i)), 0);
    }
}
