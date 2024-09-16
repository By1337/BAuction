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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class BMenuApi {
    private static Message message;
    private static Plugin owner;
    private static ThreadPoolExecutor executor;

    public static void setup(Message message, Plugin owner) {
        BMenuApi.message = message;
        BMenuApi.owner = owner;
    }

    public static void enable() {
        executor = new ThreadPoolExecutor(0, Runtime.getRuntime().availableProcessors() / 2,
                60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(),
                ThreadCreator.createWithName("bauc-menu-worker-#%d"));

        AdapterRegistry.registerPrimitiveAdapter(InventoryType.class, new AdapterEnum<>(InventoryType.class));
        AdapterRegistry.registerAdapter(Requirements.class, new AdapterRequirements());
        AdapterRegistry.registerAdapter(MenuItemBuilder.class, new AdapterMenuItemBuilder());
        AdapterRegistry.registerAdapter(Requirement.class, new AdapterIRequirement());
    }

    public static void disable() {
        AdapterRegistry.unregisterAdapter(Requirements.class);
        AdapterRegistry.unregisterAdapter(MenuItemBuilder.class);
        AdapterRegistry.unregisterAdapter(Requirement.class);
        executor.shutdown();
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

    public static ThreadPoolExecutor getExecutor() {
        return executor;
    }
}
