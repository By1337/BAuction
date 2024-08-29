package org.by1337.bauction.util.plugin;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.by1337.bauction.Main;
import org.by1337.blib.util.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;

public class PluginEnablePipeline {
    //private final List<Pair<String, ThrowableRunnable>> enableHandlers = new ArrayList<>();
    private final List<Pair<@Nullable Predicate<PluginEnablePipeline>, Pair<String, ThrowableRunnable>>> disableHandlers = new ArrayList<>();
    private final List<Pair<@Nullable Predicate<PluginEnablePipeline>, Pair<String, ThrowableRunnable>>> enableHandlers = new ArrayList<>();
    private final Set<String> enabled = new HashSet<>();
    private final Plugin plugin;

    public PluginEnablePipeline(Plugin plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        onDisable();
        onEnable();
    }

    public void onDisable() {
        for (Pair<Predicate<PluginEnablePipeline>, Pair<String, ThrowableRunnable>> pair : disableHandlers) {
            try {
                if (pair.getLeft() == null || pair.getLeft().test(this)) {
                    Main.debug("disable %s", pair.getRight().getLeft());
                    pair.getRight().getRight().run();
                }
            } catch (Throwable e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred during shutdown in the handler: " + pair.getRight().getLeft(), e);
            }
        }
        enabled.clear();
    }

    public void onEnable() {
        for (Pair<Predicate<PluginEnablePipeline>, Pair<String, ThrowableRunnable>> pair : enableHandlers) {
            try {
                if (pair.getLeft() == null || pair.getLeft().test(this)) {
                    Main.debug("enable %s", pair.getRight().getLeft());
                    pair.getRight().getRight().run();
                    enabled.add(pair.getRight().getLeft());
                }

            } catch (Throwable e) {
                plugin.getLogger().log(Level.SEVERE, "An error occurred while enabling on the handler: " + pair.getLeft(), e);
                Bukkit.getPluginManager().disablePlugin(plugin);
            }
        }
    }

    public PluginEnablePipeline disable(String name, Predicate<PluginEnablePipeline> filter, ThrowableRunnable run) {
        disableHandlers.add(Pair.of(filter, Pair.of(name, run)));
        return this;
    }

    public PluginEnablePipeline disable(String name, ThrowableRunnable run) {
        disableHandlers.add(Pair.of(null, Pair.of(name, run)));
        return this;
    }

    public PluginEnablePipeline enable(String name, Predicate<PluginEnablePipeline> filter, ThrowableRunnable run) {
        enableHandlers.add(Pair.of(filter, Pair.of(name, run)));
        return this;
    }
    public PluginEnablePipeline enable(String name, ThrowableRunnable run) {
        enableHandlers.add(Pair.of(null, Pair.of(name, run)));
        return this;
    }

    public boolean isEnabled(String... handlers) {
        for (String handler : handlers) {
            if (!enabled.contains(handler)) return false;
        }
        return true;
    }

    public interface ThrowableRunnable {
        void run() throws Throwable;
    }
}
