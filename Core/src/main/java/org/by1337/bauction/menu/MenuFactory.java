package org.by1337.bauction.menu;


import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import org.by1337.api.configuration.YamlContext;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.click.Click;
import org.by1337.bauction.menu.click.ClickType;
import org.by1337.bauction.menu.click.IClick;
import org.by1337.bauction.menu.requirement.*;
import org.by1337.bauction.menu.util.EnchantmentBuilder;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.List;

public class MenuFactory {

    public static MenuSetting create(YamlContext context) {
        List<CustomItemStack> items = context.getMap("items", CustomItemStack.class).values().stream().sorted().toList();
        String title = context.getAsString("menu_title");
        int size = context.getAsInteger("size");

        Requirements viewRequirement = context.getAs("open_requirement", Requirements.class, null);
        InventoryType type = context.getAs("type", InventoryType.class, InventoryType.CHEST);
        return new MenuSetting(items, title, size, -1, viewRequirement, type);
    }


    public static List<Integer> getSlots(List<String> list) {
        List<Integer> slots = new ArrayList<>();
        for (String str : list) {
            if (str.contains("-")) {
                String[] s = str.replace(" ", "").split("-");
                int x = Integer.parseInt(s[0]);
                int x1 = Integer.parseInt(s[1]);
                for (int i = Math.min(x, x1); i <= Math.max(x, x1); i++) {
                    slots.add(i);
                }
            } else {
                int x = Integer.parseInt(str.replace(" ", ""));
                slots.add(x);
            }
        }
        return slots;
    }

}
