package org.by1337.bauction.menu;


import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.auc.UnsoldItem;
import org.by1337.bauction.menu.click.ClickType;
import org.by1337.bauction.menu.click.IClick;
import org.by1337.bauction.menu.requirement.Requirements;
import org.by1337.bauction.menu.util.EnchantmentBuilder;

import org.by1337.bauction.util.BaseHeadHook;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.bauction.util.UniqueName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class CustomItemStack implements Comparable<CustomItemStack>, Placeholderable {
    public static final NamespacedKey MENU_ITEM_KEY = Objects.requireNonNull(NamespacedKey.fromString("bauc_menu_item"));
    private int[] slots;
    private List<String> lore;
    private final String name;
    private Map<ClickType, IClick> clicks;
    private int amount;
    private String material;
    private Requirements viewRequirement = null;
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
    private final int id;
    private final List<Placeholderable> holders = new ArrayList<>();
    private String clone;

    public CustomItemStack(int[] slots, ItemStack itemStack) {
        this.slots = slots;
        this.itemStack = itemStack;
        id = new Random().nextInt(Integer.MAX_VALUE);
        name = null;
    }

    private ItemStack itemStack = null;

    public CustomItemStack(int[] slots, List<String> lore, @Nullable String name, Map<ClickType, IClick> clicks, int amount, String material) {
        this.slots = slots;
        this.lore = lore;
        this.name = name;
        this.clicks = clicks;
        this.amount = amount;
        this.material = material;
        id = new Random().nextInt(Integer.MAX_VALUE);
    }


    @Nullable
    public ItemStack getItem(Placeholderable holder, Menu menu) {
        Placeholderable holder1 = s -> replace(holder.replace(s));
        if (viewRequirement != null && !viewRequirement.check(holder1, menu)) {
            return null;
        }

        ItemStack itemStack;
        if (this.itemStack == null) {
            String tmpMaterial = holder1.replace(material);
            if (tmpMaterial.startsWith("basehead-")) {
                itemStack = BaseHeadHook.getItem(tmpMaterial);
            } else {
                itemStack = new ItemStack(Material.valueOf(holder1.replace(tmpMaterial)));
            }
        } else {
                itemStack = this.itemStack.clone();
        }

        ItemMeta im = itemStack.getItemMeta();

        if (im == null) {
            Main.getMessage().error(new Throwable("ItemMeta is null! " + itemStack.getType()));
            itemStack = new ItemStack(Material.JIGSAW);
            im = itemStack.getItemMeta();
        }

        im.getPersistentDataContainer().set(MENU_ITEM_KEY, PersistentDataType.INTEGER, id);

        if (this.itemStack != null && im.getLore() != null) {
            List<String> tLore = new ArrayList<>(im.getLore());
            tLore.addAll(lore);
            tLore.replaceAll(holder1::replace);

            im.setLore(tLore.stream()
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .collect(Collectors.toList()));
        } else {
            List<String> tLore = new ArrayList<>(lore);
            tLore.replaceAll(holder1::replace);

            im.setLore(tLore.stream()
                    .flatMap(line -> Arrays.stream(line.split("\n")))
                    .collect(Collectors.toList()));
        }

        if (name != null)
            im.setDisplayName(holder1.replace(name));

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
        return itemStack;
    }

    public void run(InventoryClickEvent e, Menu menu) {
        Placeholderable holder1 = s -> {
            for (Placeholderable customPlaceHolder : holders) {
                s = customPlaceHolder.replace(s);
            }
            return menu.replace(s);
        };
        ClickType clickType1 = ClickType.adapter(e);
        if (clickType1 == null) return;
        IClick click = clicks.get(clickType1);
        if (click != null) {
            click.run(menu, holder1);
        }
        IClick click1 = clicks.get(ClickType.ANY_CLICK);
        if (click1 != null) {
            click1.run(menu, holder1);
        }
    }

    public void registerPlaceholder(Placeholderable holder) {
        holders.add(holder);
    }

    @Override
    public int compareTo(@NotNull CustomItemStack o) {
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

    public int getId() {
        return this.id;
    }

    public List<Placeholderable> getHolders() {
        return this.holders;
    }

    public void setSlots(int[] slots) {
        this.slots = slots;
    }

    public void setLore(List<String> lore) {
        this.lore = lore;
    }

//    public void setName(String name) {
//        this.name = name;
//    }

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

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public void setItemStack(ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    public String getClone() {
        return clone;
    }

    public void setClone(String clone) {
        this.clone = clone;
    }

    @Override
    public String replace(String s) {
        for (Placeholderable customPlaceHolder : getHolders()) {
            s = customPlaceHolder.replace(s);
        }
        return s;
    }
}
