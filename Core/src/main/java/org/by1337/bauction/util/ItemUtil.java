package org.by1337.bauction.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;

public class ItemUtil {
    private final static EnumSet<Material> SHULKERS;
    public static boolean isShulker(@Nullable ItemStack itemStack) {
        return itemStack != null && isShulker(itemStack.getType());
    }

    public static boolean isShulker(@Nullable Material material) {
       return material != null && SHULKERS.contains(material);
    }
    static {
        SHULKERS = EnumSet.noneOf(Material.class);
        for (Material value : Material.values()) {
            if (value.isBlock() && value.name().endsWith("SHULKER_BOX")){
                SHULKERS.add(value);
            }
        }
    }
}
