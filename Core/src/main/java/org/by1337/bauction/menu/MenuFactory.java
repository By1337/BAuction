package org.by1337.bauction.menu;


import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemorySection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
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

    public static MenuSetting create(FileConfiguration menuFile) {
        YamlContext context = new YamlContext(menuFile);
        List<CustomItemStack> items = context.getMap("items", CustomItemStack.class).values().stream().toList();
//        for (String key : menuFile.getConfigurationSection("items").getKeys(false)) {
//            Map<String, Object> map = menuFile.getConfigurationSection(String.format("items.%s", key)).getValues(false);
//
//            items.add(menuItemBuilder(map));
//        }

        String title = context.getAsString("menu_title");//menuFile.getString("menu_title", "&7title");
        int size = context.getAsInteger("size");// menuFile.getInt("size", 54);

       // int updateInterval = menuFile.getInt("update_interval", -1);

        Requirements viewRequirement = context.getAs("open_requirement", Requirements.class, null);
//        if (menuFile.getConfigurationSection("open_requirement") != null) {
//            viewRequirement = getRequirements(menuFile.getConfigurationSection("open_requirement").getValues(false));
//        } else {
//            viewRequirement = null;
//        }
        return new MenuSetting(items, title, size, -1, menuFile, viewRequirement);
    }

    @Deprecated(forRemoval = true)
    public static CustomItemStack menuItemBuilder(Map<String, Object> map) {
        HashMap<ClickType, IClick> clicks = new HashMap<>();

        int[] slots = getSlots(map);
        int amount = ((Number) map.getOrDefault("amount", 1)).intValue();
        String material = (String) map.getOrDefault("material", "STONE");
        String display_name = (String) map.getOrDefault("display_name", null);

        List<String> lore;

        if (map.containsKey("lore")) {
            lore = (List<String>) map.get("lore");
        } else {
            lore = new ArrayList<>();
        }


        CustomItemStack customItemStack = new CustomItemStack(slots, lore, display_name, clicks, amount, material);

        if (map.containsKey("item_flags")) {
            List<ItemFlag> flags = new ArrayList<>();
            for (Object o : (List<?>) map.get("item_flags")) {
                try {
                    flags.add(ItemFlag.valueOf(o.toString()));
                } catch (IllegalArgumentException e) {
                    Main.getMessage().error(e);
                }
            }
            customItemStack.setItemFlags(flags);
        }
        if (map.containsKey("potion_effects")) {
            List<PotionEffect> effects = new ArrayList<>();
            try {
                for (Object o : (List<?>) map.get("potion_effects")) {
                    String[] args = o.toString().split(";");
                    if (args.length != 3) {
                        Main.getMessage().error("ожидался <PotionEffectType>;<duration>;<amplifier>, а не " + o);
                        continue;
                    }
                    PotionEffectType type = Objects.requireNonNull(PotionEffectType.getByName(args[0]), "PotionEffectType is null");
                    int duration = Integer.parseInt(args[1]);
                    int amplifier = Integer.parseInt(args[2]);
                    effects.add(new PotionEffect(type, duration, amplifier));
                }
                customItemStack.setPotionEffects(effects);
            } catch (IllegalArgumentException e) {
                Main.getMessage().error(e);
            }
        }
        if (map.containsKey("rgb")) {
            try {
                String s = (String) map.get("rgb");
                s = s.replaceAll(" ", "");
                String[] args = s.split(",");
                Color color = Color.fromRGB(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
                customItemStack.setColor(color);
            } catch (IllegalArgumentException e) {
                Main.getMessage().error(e);
            }

        }
        if (map.containsKey("enchantments")) {
            List<EnchantmentBuilder> enchantments = new ArrayList<>();
            try {
                for (Object o : (List<?>) map.get("enchantments")) {
                    String[] args = o.toString().split(";");
                    if (args.length != 2) {
                        Main.getMessage().error("ожидался enchantmentid;level, а не " + o);
                        continue;
                    }
                    Enchantment type = Objects.requireNonNull(Enchantment.getByKey(NamespacedKey.minecraft(args[0])), "Enchantment is null");
                    int level = Integer.parseInt(args[1]);
                    enchantments.add(new EnchantmentBuilder(level, type));
                }
                customItemStack.setEnchantments(enchantments);
            } catch (IllegalArgumentException e) {
                Main.getMessage().error(e);
            }
        }
        customItemStack.setHideEnchantments((Boolean) map.getOrDefault("hide_enchantments", false));
        customItemStack.setHideAttributes((Boolean) map.getOrDefault("hide_attributes", false));
        customItemStack.setHideEffects((Boolean) map.getOrDefault("hide_effects", false));
        customItemStack.setHideUnbreakable((Boolean) map.getOrDefault("hide_unbreakable", false));
        customItemStack.setUnbreakable((Boolean) map.getOrDefault("unbreakable", false));

        if (map.containsKey("model_data")) {
            int model_data = ((Number) map.get("model_data")).intValue();
            customItemStack.setModelData(model_data);
        }

        int priority = ((Number) map.getOrDefault("priority", 0)).intValue();
        customItemStack.setPriority(priority);


        if (map.containsKey("view_requirement")) {
            MemorySection memorySection = (MemorySection) map.get("view_requirement");
            Requirements viewRequirement = getRequirements(memorySection.getValues(false));
            customItemStack.setViewRequirement(viewRequirement);
        }


        for (ClickType clickType : ClickType.values()) {
            if (map.containsKey(clickType.getConfigKeyClick())) {
                List<String> commands = (List<String>) map.get(clickType.getConfigKeyClick());
                Requirements requirements;

                if (map.containsKey(clickType.getConfigKeyRequirement())) {
                    MemorySection memorySection = (MemorySection) map.get(clickType.getConfigKeyRequirement());
                    requirements = getRequirements(memorySection.getValues(false));
                } else {
                    requirements = null;
                }
                Click click = new Click(commands.toArray(new String[0]), requirements, clickType);
                clicks.put(click.getClickType(), click);
            }
        }
        return customItemStack;
    }

    @Nullable
    public static Requirements getRequirements(Map<String, Object> map) {
        try {
            MemorySection memRequirements = (MemorySection) Objects.requireNonNull(map.getOrDefault("requirements", null), "requirements is null");
            Map<String, Object> requirements = memRequirements.getValues(false);

            List<IRequirement> requirementList = new ArrayList<>();

            for (String key : requirements.keySet()) {
                MemorySection memRequirement = (MemorySection) requirements.get(key);
                Map<String, Object> requirement = memRequirement.getValues(false);
                String type = (String) Objects.requireNonNull(requirement.getOrDefault("type", null), "type is null");

                if (type.equalsIgnoreCase("string equals") || type.equalsIgnoreCase("sq")) {
                    String input = (String) Objects.requireNonNull(requirement.getOrDefault("input", null), "input is null");
                    String output = (String) Objects.requireNonNull(requirement.getOrDefault("output", null), "output is null");
                    RequirementStringEquals stringEquals = new RequirementStringEquals(input, output, key);
                    requirementList.add(stringEquals);
                    continue;
                }

                if (type.equalsIgnoreCase("string contains") || type.equalsIgnoreCase("sc")) {
                    String input = (String) Objects.requireNonNull(requirement.getOrDefault("input", null), "input is null");
                    String input2 = (String) Objects.requireNonNull(requirement.getOrDefault("input2", null), "input2 is null");
                    String output = (String) Objects.requireNonNull(requirement.getOrDefault("output", null), "output is null");
                    RequirementStringContains stringEquals = new RequirementStringContains(input, input2, output, key);
                    requirementList.add(stringEquals);
                    continue;
                }

                if (type.equalsIgnoreCase("has permission") || type.equalsIgnoreCase("hp")) {
                    String permission = (String) Objects.requireNonNull(requirement.getOrDefault("permission", null), "permission is null");
                    RequirementHasPermission requirementHasPermission = new RequirementHasPermission(permission);
                    requirementList.add(requirementHasPermission);
                    continue;
                }
            }
            String[] denyCommands;
            if (map.containsKey("deny_commands")) {
                List<String> list = (List<String>) map.get("deny_commands");
                denyCommands = list.toArray(new String[0]);
            } else {
                denyCommands = new String[0];
            }
            return new Requirements(requirementList, denyCommands);
        } catch (NullPointerException e) {
            Main.getMessage().error(e);
            return null;
        }
    }

    public static int[] getSlots(Map<String, Object> map) {
        List<Integer> slots = new ArrayList<>();
        try {
            if (map.containsKey("slot")) {
                slots.add(((Number) map.getOrDefault("slot", 0)).intValue());
            }
            if (map.containsKey("slots")) {
                Object slotsObject = map.get("slots");
                if (slotsObject instanceof List<?> slotsList) {
                    for (Object obj : slotsList) {
                        if (obj instanceof Integer) {
                            slots.add((Integer) obj);
                        } else if (obj instanceof String str) {
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

    public static List<Integer> getSlots(FileConfiguration cfg, String key) {
        List<Integer> slots = new ArrayList<>();
        List<?> slotsList = cfg.getList(key);
        assert slotsList != null;
        for (Object obj : slotsList) {
            if (obj instanceof Integer) {
                slots.add((Integer) obj);
            } else if (obj instanceof String str) {
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
        return slots;
    }

}
