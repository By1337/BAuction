package org.by1337.bauction.menu.util;

import org.bukkit.enchantments.Enchantment;

public class EnchantmentBuilder {
    private int level;
    private Enchantment enchantment;

    public EnchantmentBuilder(int level, Enchantment enchantment) {
        this.level = level;
        this.enchantment = enchantment;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Enchantment getEnchantment() {
        return enchantment;
    }

    public void setEnchantment(Enchantment enchantment) {
        this.enchantment = enchantment;
    }
}