package org.by1337.bmenu.config.adapter;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.configuration.adapter.ClassAdapter;
import org.by1337.bmenu.BMenuApi;
import org.by1337.bmenu.menu.MenuItemBuilder;
import org.by1337.bmenu.menu.click.Click;
import org.by1337.bmenu.menu.click.ClickType;
import org.by1337.bmenu.menu.click.IClick;
import org.by1337.bmenu.menu.requirement.Requirement;
import org.by1337.bmenu.menu.requirement.Requirements;
import org.by1337.bmenu.menu.util.EnchantmentBuilder;

import java.util.*;

public class AdapterMenuItemBuilder implements ClassAdapter<MenuItemBuilder> {
    @Override
    public ConfigurationSection serialize(MenuItemBuilder obj, YamlContext context) {
        throw new IllegalArgumentException();
    }

    @Override
    public MenuItemBuilder deserialize(YamlContext context) {
        int amount = context.getAsInteger("amount", 1);
        String material = context.getAsString("material", "STONE");
        String displayName = context.getAsString("display_name", null);

        List<String> lore = context.getList("lore", String.class, new ArrayList<>());

        List<ItemFlag> flags = context.getList("item_flags", ItemFlag.class, new ArrayList<>());

        List<PotionEffect> effects = context.getList("potion_effects", String.class, new ArrayList<>()).stream().map(str -> {
            String[] args = str.split(";");
            if (args.length != 3) {
                BMenuApi.getMessage().error("expected <PotionEffectType>;<duration>;<amplifier>, not " + str);
                return null;
            }
            PotionEffectType type = Objects.requireNonNull(PotionEffectType.getByName(args[0].toLowerCase(Locale.ENGLISH)), "PotionEffectType is null");
            int duration = Integer.parseInt(args[1]);
            int amplifier = Integer.parseInt(args[2]);
            return new PotionEffect(type, duration, amplifier);
        }).filter(Objects::nonNull).toList();

        Color color = context.getAs("hex", Color.class, null);

        List<EnchantmentBuilder> enchantments = context.getList("enchantments", String.class, new ArrayList<>()).stream().map(str -> {
                    String[] args = str.split(";");
                    if (args.length != 2) {
                        BMenuApi.getMessage().error("was expected to be enchantmentid;level, not " + str);
                        return null;
                    }
                    Enchantment type = Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(args[0].toLowerCase(Locale.ENGLISH))), "Enchantment is null");
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
        Requirements anyClickRequirement;
        if (context.getHandle().contains("any_click_requirement")) {
            anyClickRequirement = anyClickRequirementDeserialize(context.getAs("any_click_requirement", YamlContext.class));
        } else {
            anyClickRequirement = null;
        }

        Map<ClickType, IClick> clicks = new HashMap<>();

        for (ClickType clickType : ClickType.values()) {
            if (context.getHandle().contains(clickType.getConfigKeyClick())) {
                List<String> commands = context.getList(clickType.getConfigKeyClick(), String.class, new ArrayList<>());
                Requirements requirements = context.getAs(clickType.getConfigKeyRequirement(), Requirements.class, null);
                Click click = new Click(commands, requirements, clickType);
                clicks.put(click.getClickType(), click);
            }
        }

        int[] slots = getSlots(context);

        MenuItemBuilder menuItemBuilder = new MenuItemBuilder(slots, Collections.unmodifiableList(lore), displayName, clicks, amount, material, anyClickRequirement);
        menuItemBuilder.setItemFlags(flags);
        menuItemBuilder.setPotionEffects(effects);
        menuItemBuilder.setColor(color);

        menuItemBuilder.setHideEnchantments(hideEnchantments);
        menuItemBuilder.setHideAttributes(hideAttributes);
        menuItemBuilder.setHideEffects(hideEffects);
        menuItemBuilder.setHideUnbreakable(hideUnbreakable);
        menuItemBuilder.setUnbreakable(unbreakable);
        menuItemBuilder.setModelData(model_data);
        menuItemBuilder.setPriority(priority);
        menuItemBuilder.setViewRequirement(viewRequirement);
        menuItemBuilder.setEnchantments(enchantments);

        return menuItemBuilder;
    }

    public Requirements anyClickRequirementDeserialize(YamlContext context) {
        List<Requirement> requirementList = context.getMap("requirements", Requirement.class, new HashMap<>()).values().stream().toList();
        List<String> denyCommands = Collections.unmodifiableList(context.getList("commands", String.class, new ArrayList<>()));
        return new Requirements(requirementList, denyCommands);
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
            BMenuApi.getMessage().error(e);
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
