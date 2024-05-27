package org.by1337.bmenu.menu;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.chat.placeholder.MultiPlaceholder;
import org.by1337.bmenu.BMenuApi;
import org.by1337.bmenu.basehead.BaseHeadHook;
import org.by1337.bmenu.menu.click.ClickType;
import org.by1337.bmenu.menu.click.IClick;
import org.by1337.bmenu.menu.requirement.Requirements;
import org.by1337.bmenu.menu.util.EnchantmentBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MenuItemBuilder implements Comparable<MenuItemBuilder> {
    public static final NamespacedKey MENU_ITEM_KEY = Objects.requireNonNull(NamespacedKey.fromString("bmenu_item"));
    private int[] slots;
    private List<String> lore;
    private final String name;
    private Map<ClickType, IClick> clicks;
    private int amount;
    private String material;
    private Requirements viewRequirement = null;
    private Requirements anyClickRequirement = null;
    private int modelData = 0;
    private List<ItemFlag> itemFlags = new ArrayList<>();
    private List<PotionEffect> potionEffects = new ArrayList<>();
    private Color color = null;
    private int priority = 0;
    private List<EnchantmentBuilder> enchantments = new ArrayList<>();
    private boolean hideEnchantments = false;
    private boolean hideAttributes = false;
    private boolean hideEffects = false;
    private boolean hideUnbreakable = false;
    private boolean unbreakable = false;

    public MenuItemBuilder(int[] slots, List<String> lore, @Nullable String name, Map<ClickType, IClick> clicks, int amount, String material, Requirements anyClickRequirement) {
        this.anyClickRequirement = anyClickRequirement;
        this.slots = slots;
        this.lore = lore;
        this.name = name;
        this.clicks = clicks;
        this.amount = amount;
        this.material = material;
    }

    public MenuItem build(Menu menu) {
        return build(menu, null);
    }

    public MenuItem build(Menu menu, @Nullable ItemStack itemStack, Placeholderable... placeholderables) {
        MultiPlaceholder placeholder = new MultiPlaceholder(placeholderables);
        placeholder.add(menu);

        if (viewRequirement != null && !viewRequirement.check(placeholder, menu.viewer)) {
            menu.runCommands(viewRequirement.getDenyCommands());
            return null;
        }
        if (itemStack == null) {
            String tmpMaterial = placeholder.replace(material);
            if (tmpMaterial.startsWith("basehead-")) {
                itemStack = BaseHeadHook.getItem(tmpMaterial);
            } else {
                itemStack = new ItemStack(Material.valueOf(menu.replace(tmpMaterial).toUpperCase(Locale.ENGLISH)));
            }
        } else {
            itemStack = itemStack.clone();
        }


        ItemMeta im = itemStack.getItemMeta();
        if (im == null) {
            BMenuApi.getMessage().error(new Throwable("ItemMeta is null! " + itemStack.getType()));
            itemStack = new ItemStack(Material.JIGSAW);
            im = itemStack.getItemMeta();
        }

        im.getPersistentDataContainer().set(MENU_ITEM_KEY, PersistentDataType.INTEGER, 1);

        List<Component> lore = new ArrayList<>(getOrDefault(im.lore(), ArrayList::new));
        for (String s : this.lore) {
            String s1 = placeholder.replace(s);
            if (s1.contains("\n")) {
                for (String string : s1.split("\n")) {
                    lore.add(BMenuApi.getMessage().componentBuilder(string).decoration(TextDecoration.ITALIC, false));
                }
            } else {
                lore.add(BMenuApi.getMessage().componentBuilder(s1).decoration(TextDecoration.ITALIC, false));
            }
        }
        im.lore(lore);

        if (name != null)
            im.displayName(BMenuApi.getMessage().componentBuilder(placeholder.replace(name)).decoration(TextDecoration.ITALIC, false));

        for (ItemFlag itemFlag : itemFlags)
            im.addItemFlags(itemFlag);

        for (PotionEffect potionEffect : potionEffects) {
            ((PotionMeta) im).addCustomEffect(potionEffect, true);
        }
        if (color != null) {
            ((PotionMeta) im).setColor(color);
        }

        for (EnchantmentBuilder enchantmentBuilder : enchantments) {
            im.addEnchant(enchantmentBuilder.getEnchantment(), enchantmentBuilder.getLevel(), true);
        }
        if (modelData != 0) {
            im.setCustomModelData(modelData);
        }
        if (hideEnchantments) {
            im.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }
        if (hideAttributes) {
            im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        }
        if (hideEffects) {
            im.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS);
        }
        if (hideUnbreakable) {
            im.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        }
        if (unbreakable) {
            im.setUnbreakable(true);
        }
        itemStack.setItemMeta(im);
        itemStack.setAmount(amount);

        return new MenuItem(
                slots, itemStack, clicks, placeholder, anyClickRequirement
        );
    }

    public <T> T getOrDefault(@Nullable T value, Supplier<T> supplier) {
        if (value == null) return supplier.get();
        return value;
    }


    @Override
    public int compareTo(@NotNull MenuItemBuilder o) {
        return Integer.compare(priority, o.getPriority());
    }


    public int[] getSlots() {
        return this.slots;
    }

    public List<String> getLore() {
        return this.lore;
    }

    public String getName() {
        return this.name;
    }

    public Map<ClickType, IClick> getClicks() {
        return this.clicks;
    }

    public int getAmount() {
        return this.amount;
    }

    public String getMaterial() {
        return this.material;
    }

    public Requirements getViewRequirement() {
        return this.viewRequirement;
    }

    public int getModelData() {
        return this.modelData;
    }

    public List<ItemFlag> getItemFlags() {
        return this.itemFlags;
    }

    public List<PotionEffect> getPotionEffects() {
        return this.potionEffects;
    }

    public Color getColor() {
        return this.color;
    }

    public int getPriority() {
        return this.priority;
    }

    public List<EnchantmentBuilder> getEnchantments() {
        return this.enchantments;
    }

    public boolean isHideEnchantments() {
        return this.hideEnchantments;
    }

    public boolean isHideAttributes() {
        return this.hideAttributes;
    }

    public boolean isHideEffects() {
        return this.hideEffects;
    }

    public boolean isHideUnbreakable() {
        return this.hideUnbreakable;
    }

    public boolean isUnbreakable() {
        return this.unbreakable;
    }


    public void setSlots(int[] slots) {
        this.slots = slots;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

    public void setClicks(HashMap<ClickType, IClick> clicks) {
        this.clicks = clicks;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public void setViewRequirement(Requirements viewRequirement) {
        this.viewRequirement = viewRequirement;
    }

    public void setModelData(int modelData) {
        this.modelData = modelData;
    }

    public void setItemFlags(List<ItemFlag> itemFlags) {
        this.itemFlags = itemFlags;
    }

    public void setPotionEffects(List<PotionEffect> potionEffects) {
        this.potionEffects = potionEffects;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public void setEnchantments(List<EnchantmentBuilder> enchantments) {
        this.enchantments = enchantments;
    }

    public void setHideEnchantments(boolean hideEnchantments) {
        this.hideEnchantments = hideEnchantments;
    }

    public void setHideAttributes(boolean hideAttributes) {
        this.hideAttributes = hideAttributes;
    }

    public void setHideEffects(boolean hideEffects) {
        this.hideEffects = hideEffects;
    }

    public void setHideUnbreakable(boolean hideUnbreakable) {
        this.hideUnbreakable = hideUnbreakable;
    }

    public void setUnbreakable(boolean unbreakable) {
        this.unbreakable = unbreakable;
    }

}
