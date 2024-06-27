package org.by1337.bmenu;

import org.bukkit.event.inventory.InventoryType;
import org.bukkit.plugin.Plugin;
import org.by1337.bauction.util.threading.ThreadCreator;
import org.by1337.blib.chat.util.Message;
import org.by1337.blib.configuration.adapter.AdapterRegistry;
import org.by1337.blib.configuration.adapter.impl.primitive.AdapterEnum;
import org.by1337.bmenu.config.adapter.AdapterIRequirement;
import org.by1337.bmenu.config.adapter.AdapterMenuItemBuilder;
import org.by1337.bmenu.config.adapter.AdapterRequirements;
import org.by1337.bmenu.menu.MenuItemBuilder;
import org.by1337.bmenu.menu.requirement.Requirement;
import org.by1337.bmenu.menu.requirement.Requirements;

import java.util.concurrent.ThreadFactory;

public class BMenuApi {
    private static Message message;
    private static Plugin owner;
    private static ThreadFactory threadFactory;
    public static void setup(Message message, Plugin owner){
        BMenuApi.message = message;
        BMenuApi.owner = owner;
    }
    public static void enable(){
        threadFactory = ThreadCreator.createWithName("Menu click executor #%d");
        AdapterRegistry.registerPrimitiveAdapter(InventoryType.class, new AdapterEnum<>(InventoryType.class));
        AdapterRegistry.registerAdapter(Requirements.class, new AdapterRequirements());
        AdapterRegistry.registerAdapter(MenuItemBuilder.class, new AdapterMenuItemBuilder());
        AdapterRegistry.registerAdapter(Requirement.class, new AdapterIRequirement());
    }

    public static void disable(){
        AdapterRegistry.unregisterAdapter(Requirements.class);
        AdapterRegistry.unregisterAdapter(MenuItemBuilder.class);
        AdapterRegistry.unregisterAdapter(Requirement.class);
    }

    public static Message getMessage() {
        return message;
    }
    public static Plugin getInstance() {
        return owner;
    }
    public static Plugin getOwner() {
        return owner;
    }

    public static ThreadFactory getThreadFactory() {
        return threadFactory;
    }
}
