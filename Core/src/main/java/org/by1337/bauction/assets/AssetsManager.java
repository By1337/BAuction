package org.by1337.bauction.assets;

import org.bukkit.plugin.Plugin;
import org.by1337.blib.nbt.NBTParser;
import org.by1337.blib.nbt.NBTToString;
import org.by1337.blib.nbt.NBTToStringStyle;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.by1337.blib.util.Version;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class AssetsManager {
    private final Plugin plugin;
    private final File dataFolder;
    private final String locale;
    private final ItemNames itemNames;

    public AssetsManager(Plugin plugin, String locale) throws IOException {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "assets");
        this.locale = locale;
        if (!dataFolder.mkdirs() && !dataFolder.exists())
            throw new IllegalStateException("Failed to create directory!");
        itemNames = new ItemNames();
    }

    public ItemNames getItemNames() {
        return itemNames;
    }

    public class ItemNames {
        private Map<String, String> translation;
        private Map<String, String> translationTabCompleterStyle;

        public ItemNames() throws IOException {
            if (!locale.contains("_")){
                plugin.getLogger().log(Level.SEVERE, "Invalid locale syntax! Example: ru_ru, en_us. locale=" + locale);
                throw new IllegalArgumentException("Invalid locale syntax! Example: ru_ru, en_us. locale=" + locale);
            }
            try {
                downloadAndApply();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to download lang!", e);
                new File(dataFolder, locale + ".json").delete();
                downloadAndApply();
            }

        }
        private void downloadAndApply() throws IOException {
            File f = AssetsDownloader.downloadFrom(
                    AssetsDownloader.SITE + "/" + Version.VERSION.getVer() + "/" + AssetsDownloader.LANG_PATH + "/" + locale + ".json",
                    new File(dataFolder, locale + ".json")
            ).join();

            CompoundTag compoundTag = NBTParser.parseAsCompoundTag(Files.readString(f.toPath(), StandardCharsets.UTF_8));
            if (clear(compoundTag)) {
                compoundTag.putString("_fixed_", "true");
                Files.writeString(f.toPath(),
                        NBTToString.toString(compoundTag, NBTToStringStyle.JSON_STYLE_COMPACT)
                );
            }
            compoundTag.remove("_fixed_");
            translation = new HashMap<>(compoundTag.getTags().size());
            translationTabCompleterStyle = new HashMap<>(compoundTag.getTags().size());
            for (String s : compoundTag.getTags().keySet()) {
                translationTabCompleterStyle.put(s, compoundTag.getAsString(s).toLowerCase().replace(" ", "_"));
                translation.put(s, compoundTag.getAsString(s).toLowerCase());
            }
        }

        private boolean clear(CompoundTag compoundTag) {
            if (compoundTag.has("_fixed_")) {
                return false;
            }
            boolean hasRemoved = false;
            for (String s : compoundTag.getTags().keySet().toArray(new String[0])) {
                if (!s.startsWith("item.minecraft.") && !s.startsWith("block.minecraft.")) {
                    compoundTag.remove(s);
                    hasRemoved = true;
                } else if (
                        (s.startsWith("block.minecraft.") && s.substring("block.minecraft.".length()).contains(".")) ||
                        (s.startsWith("item.minecraft.") && s.substring("item.minecraft.".length()).contains("."))
                ) {
                    compoundTag.remove(s);
                    hasRemoved = true;

                }
            }
            if (hasRemoved) {
                for (String s : compoundTag.getTags().keySet().toArray(new String[0])) {
                    var data = compoundTag.getAsString(s);
                    compoundTag.remove(s);
                    if (s.startsWith("block.minecraft.")) {
                        s = s.substring("block.minecraft.".length());
                    } else {
                        s = s.substring("item.minecraft.".length());
                    }
                    compoundTag.putString(s, data);
                }
            }
            return hasRemoved;
        }

        public Map<String, String> getTranslation() {
            return translation;
        }

        public Map<String, String> getTranslationTabCompleterStyle() {
            return translationTabCompleterStyle;
        }
    }
}
