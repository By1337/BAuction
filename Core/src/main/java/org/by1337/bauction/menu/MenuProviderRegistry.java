package org.by1337.bauction.menu;

import org.bukkit.entity.Player;
import org.by1337.bauction.menu.menu.*;
import org.by1337.bauction.util.OptionParser;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class MenuProviderRegistry {
    private static final MenuProviderRegistry INSTANCE = new MenuProviderRegistry();
    private final Map<String, MenuCreator> LOOKUP = new HashMap<>();
    public static final MenuCreator DEFAULT = register("default", SimpleMenu::new);
    public static final MenuCreator MAIN_MENU = register("main_menu", MainMenu::new);
    public static final MenuCreator BUY_CONFIRM = register("buy_confirm", BuyConfirmMenu::new);

    private MenuProviderRegistry() {
    }

    public static MenuCreator register(String name, MenuCreator creator) {
        if (INSTANCE.LOOKUP.containsKey(name)) {
            throw new IllegalStateException();
        }
        INSTANCE.LOOKUP.put(name, creator);
        return creator;
    }

    @Nullable
    public static MenuCreator getByName(String name) {
        return INSTANCE.LOOKUP.get(name);
    }

    public interface MenuCreator {
        Menu create(MenuSetting setting, Player player, @Nullable Menu previousMenu, OptionParser optionParser);
    }
}
