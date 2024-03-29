package org.by1337.bauction.config.adapter;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.CustomItemStack;
import org.by1337.bauction.menu.click.Click;
import org.by1337.bauction.menu.click.ClickType;
import org.by1337.bauction.menu.click.IClick;
import org.by1337.bauction.menu.requirement.Requirements;
import org.by1337.bauction.menu.util.EnchantmentBuilder;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.adapter.ClassAdapter;

import java.util.*;

public class AdapterCustomItemStack implements ClassAdapter<CustomItemStack> {
    @Override
    public ConfigurationSection serialize(CustomItemStack obj, YamlContext context) {
        throw new IllegalArgumentException();
    }

    @Override
    public CustomItemStack deserialize(YamlContext context) {
        int amount = context.getAsInteger("amount", 1);
        String material = context.getAsString("material", "STONE");
        String displayName = context.getAsString("display_name", null);

        List<String> lore = context.getList("lore", String.class, new ArrayList<>());

        List<ItemFlag> flags = context.getList("item_flags", ItemFlag.class, new ArrayList<>());

        List<PotionEffect> effects = context.getList("potion_effects", String.class, new ArrayList<>()).stream().map(str -> {
            String[] args = str.split(";");
            if (args.length != 3) {
                Main.getMessage().error("ожидался <PotionEffectType>;<duration>;<amplifier>, а не " + str);
                return null;
            }
            PotionEffectType type = Objects.requireNonNull(PotionEffectType.getByName(args[0]), "PotionEffectType is null");
            int duration = Integer.parseInt(args[1]);
            int amplifier = Integer.parseInt(args[2]);
            return new PotionEffect(type, duration, amplifier);
        }).filter(Objects::nonNull).toList();

        Color color = context.getAs("hex", Color.class, null);

        List<EnchantmentBuilder> enchantments = context.getList("enchantments", String.class, new ArrayList<>()).stream().map(str -> {
                    String[] args = str.split(";");
                    if (args.length != 2) {
                        Main.getMessage().error("ожидался enchantmentid;level, а не " + str);
                        return null;
                    }
                    Enchantment type = Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(args[0])), "Enchantment is null");
                    int level = Integer.parseInt(args[1]);
                    return new EnchantmentBuilder(level, type);
                }
        ).filter(Objects::nonNull).toList();

        boolean hideEnchantments = context.getAsBoolean("hide_enchantments", false);
        boolean hideAttributes = context.getAsBoolean("hide_attributes", false);
        boolean hideEffects = context.getAsBoolean("hide_effects", false);
        boolean hideUnbreakable = context.getAsBoolean("hide_unbreakable", false);
        boolean unbreakable = context.getAsBoolean("unbreakable", false);


        int model_data = context.getAsInteger("model_data", 0);

        int priority = context.getAsInteger("priority", 0);

        Requirements viewRequirement = context.getAs("view_requirement", Requirements.class, null);

        Map<ClickType, IClick> clicks = new HashMap<>();

        for (ClickType clickType : ClickType.values()) {
            if (context.getHandle().contains(clickType.getConfigKeyClick())) {
                List<String> commands = context.getList(clickType.getConfigKeyClick(), String.class, new ArrayList<>());
                Requirements requirements = context.getAs(clickType.getConfigKeyRequirement(), Requirements.class, null);
                Click click = new Click(commands.toArray(new String[0]), requirements, clickType);
                clicks.put(click.getClickType(), click);
            }
        }

        int[] slots = getSlots(context);

        CustomItemStack customItemStack = new CustomItemStack(slots, Collections.unmodifiableList(lore), displayName, clicks, amount, material);
        customItemStack.setItemFlags(flags);
        customItemStack.setPotionEffects(effects);
        customItemStack.setColor(color);

        customItemStack.setHideEnchantments(hideEnchantments);
        customItemStack.setHideAttributes(hideAttributes);
        customItemStack.setHideEffects(hideEffects);
        customItemStack.setHideUnbreakable(hideUnbreakable);
        customItemStack.setUnbreakable(unbreakable);
        customItemStack.setModelData(model_data);
        customItemStack.setPriority(priority);
        customItemStack.setViewRequirement(viewRequirement);
        customItemStack.setEnchantments(enchantments);

        return customItemStack;
    }

    public static int[] getSlots(YamlContext context) {
        List<Integer> slots = new ArrayList<>();
        try {
            if (context.getHandle().contains("slot")) {
                slots.add(context.getAsInteger("slot"));
            }
            if (context.getHandle().contains("slots")) {
                for (String str : context.getList("slots", String.class)) {
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
            }
        } catch (Exception e) {
            Main.getMessage().error(e);
            return new int[]{0};
        }
        if (slots.isEmpty()) {
            return new int[]{0};
        }
        int[] slot = new int[slots.size()];
        for (int x = 0; x < slots.size(); x++) {
            slot[x] = slots.get(x);
        }
        return slot;
    }

}
