package org.by1337.bauction.menu;


import org.bukkit.event.inventory.InventoryType;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.NameKey;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.requirement.Requirements;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MenuFactory {

    public static MenuSetting create(YamlContext context) {
        List<MenuItemBuilder> items = context.getMap("items", MenuItemBuilder.class).values().stream().sorted().toList();
        String title = context.getAsString("menu_title");
        int size = context.getAsInteger("size");
        if (size % 9 != 0 || size < 9){
            Main.getMessage().error("size должен быть кратен 9 и больше чем 9");
            size = 54;
        }
        NameKey id = Objects.requireNonNull(context.getAsNameKey("id", null), "id должен быть указан!");
        List<String> open_commands = context.getList("open_commands", String.class, new ArrayList<>());

        Requirements viewRequirement = context.getAs("open_requirement", Requirements.class, null);
        InventoryType type = context.getAs("type", InventoryType.class, InventoryType.CHEST);
        return new MenuSetting(items, title, size, viewRequirement, type, open_commands, context, id);
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
