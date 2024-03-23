package org.by1337.bauction.menu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.bauction.Main;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.NameKey;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class MenuLoader {
    private final Map<NameKey, MenuSetting> menus;

    private final Plugin plugin;
    public MenuLoader(Plugin plugin) {
        menus = new HashMap<>();
        this.plugin = plugin;
    }
    public void load() {
        menus.clear();
        File file = new File(plugin.getDataFolder() + "/menu");
        if (!file.exists()) {
            file.mkdir();
            plugin.saveResource("menu/mainMenu.yml", true);
            plugin.saveResource("menu/buyConfirmMenu.yml", true);
        }
        for (File file1 : getFiles(file)) {
            if (!file1.getName().endsWith(".yml")) continue;
            try {
                YamlConfiguration cfg = new YamlConfiguration();
                cfg.load(file1);
                YamlContext context = new YamlContext(cfg);
                MenuSetting setting = MenuFactory.create(context);
                menus.put(setting.getId(), setting);
            } catch (Exception e) {
                Main.getMessage().error(e);
            }
        }
    }

    private List<File> getFiles(File folder) {
        List<File> files = new ArrayList<>();
        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                files.addAll(getFiles(file));
            } else {
                files.add(file);
            }
        }
        return files;
    }

    @Nullable
    public MenuSetting getMenu(String id) {
        return getMenu(new NameKey(id));
    }
    @Nullable
    public MenuSetting getMenu(NameKey id) {
        return menus.get(id);
    }

    public Set<NameKey> getMenus() {
        return menus.keySet();
    }

    public void clearCommandMap() {
        menus.clear();
    }
}
