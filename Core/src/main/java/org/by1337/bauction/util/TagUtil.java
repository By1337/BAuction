package org.by1337.bauction.util;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.by1337.bauction.db.MemorySellItem;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TagUtil {
    public static HashSet<String> getTags(@NotNull ItemStack itemStack) {
        List<String> list = new ArrayList<>();

        Material material = itemStack.getType();
        list.add(material.name());
        ItemMeta im = itemStack.getItemMeta();
        itemStack.getEnchantments().keySet().forEach(e -> list.add(e.getKey().getKey()));
        list.addAll(getTags(material));

        if (im != null) {
            if (im instanceof PotionMeta potionMeta) {
                for (PotionEffect potionEffect : potionMeta.getCustomEffects()) {
                    list.add(potionEffect.getType().getName());
                }
                list.add(potionMeta.getBasePotionData().getType().name());
            }
            for (NamespacedKey key : im.getPersistentDataContainer().getKeys()) {
                list.add(key.getKey());
            }
            // todo это не работает и мне сейчас лень об этом заботиться
            if (im instanceof EnchantmentStorageMeta enchantmentStorageMeta) {
                System.out.println("ok");
                System.out.println(enchantmentStorageMeta.getEnchants().size());

                enchantmentStorageMeta.getEnchants().keySet().forEach(e -> list.add(e.getKey().getKey()));
            }
        }
        list.replaceAll(String::toLowerCase);
        return new HashSet<>(list);
    }

    public static List<String> getTags(Material material) {
        List<String> list = new ArrayList<>();

        if (material.isFlammable()) list.add(Tags.IS_FLAMMABLE.getTag());
        if (material.isBurnable()) list.add(Tags.IS_BURNABLE.getTag());
        if (material.isFuel()) list.add(Tags.IS_FUEL.getTag());
        if (material.hasGravity()) list.add(Tags.HAS_GRAVITY.getTag());
        if (material.isSolid()) list.add(Tags.IS_SOLID.getTag());
        if (material.isRecord()) list.add(Tags.IS_RECORD.getTag());
        if (material.isEdible()) list.add(Tags.IS_EDIBLE.getTag());
        if (material.isBlock()) list.add(Tags.IS_BLOCK.getTag());
        return list;
    }

    public static boolean matchesCategory(Category category, MemorySellItem sellItem) {
        if (category.tags().contains("any")) return true;
        for (String s : sellItem.getTags()) {
            if (category.tags().contains(s))
                return true;
        }
        return false;
    }

    public enum Tags {
        IS_FLAMMABLE("is_flammable"),
        IS_BURNABLE("is_burnable"),
        IS_FUEL("is_fuel"),
        HAS_GRAVITY("has_gravity"),
        IS_SOLID("is_solid"),
        IS_RECORD("is_record"),
        IS_EDIBLE("is_edible"),
        IS_BLOCK("is_block"),
        IS_TOOL("is_tool"),
        IS_ARMOR("is_armor"),
        IS_WEAPON("is_weapon"),
        IS_SPAWN_AGG("is_spawn_agg");

        private final String tag;

        Tags(String tag) {
            this.tag = tag;
        }

        public String getTag() {
            return tag;
        }
    }

}
