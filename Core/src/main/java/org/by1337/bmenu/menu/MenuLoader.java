package org.by1337.bmenu.menu;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.util.NameKey;
import org.by1337.bmenu.BMenuApi;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.*;

public class MenuLoader {
    private final Map<NameKey, MenuSetting> menus;
    private final Plugin plugin;
    private final File menuFolder;

    public MenuLoader(Plugin plugin, File menuFolder) {
        this.menuFolder = menuFolder;
        menus = new HashMap<>();
        this.plugin = plugin;
    }

    public void reload() {
        load();
    }

    public void load() {
        menus.clear();
        for (File file1 : getFiles(menuFolder)) {
            if (!file1.getName().endsWith(".yml")) continue;
            try {
                YamlConfiguration cfg = new YamlConfiguration();
                cfg.load(file1);
                YamlContext context = new YamlContext(cfg);
                MenuSetting setting = MenuFactory.create(context, this);
                menus.put(setting.getId(), setting);
            } catch (Exception e) {
                BMenuApi.getMessage().error(e);
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

    public Plugin getPlugin() {
        return plugin;
    }

    public File getMenuFolder() {
        return menuFolder;
    }
}
