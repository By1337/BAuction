package org.by1337.bauction;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.*;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.scheduler.BukkitScheduler;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import static org.mockito.Mockito.*;

public class PluginBootstrap {
    private Main instance;
    private File dataFolder;
    private PluginDescriptionFile descriptionFile;
    private Server server;
    private BukkitScheduler scheduler;
    private PluginManager pluginManager;
    private SimpleServicesManager servicesManager;
    private TestEconomy economy;

    public void init() {
        if (instance != null) return;
        initServer();
        Main.RUNNING_IN_IDE = true;
        Main.DEBUG_MODE = true;
        System.setProperty("bauction.db.memory", "true");

        dataFolder = com.google.common.io.Files.createTempDir();


        descriptionFile = new PluginDescriptionFile("BAuction", "test", Main.class.getCanonicalName());


        Main main = new Main(mock(JavaPluginLoader.class), descriptionFile, dataFolder, new File(dataFolder, "nop"));
        setField(JavaPlugin.class, main, "server", server);
        when(server.getPluginCommand(any(String.class))).thenAnswer(invocation -> {
            PluginCommand command = mock(PluginCommand.class);
            when(command.getPlugin()).thenReturn(main);
            return command;
        });

        instance = main;

        Main.setInstance(instance);
        instance.onLoad();
        instance.onEnable();
    }

    private void initServer() {
        server = mock(Server.class);
        setStatic(Bukkit.class, "server", server);

        scheduler = mock(BukkitScheduler.class);
        pluginManager = mock(PluginManager.class);

        when(server.getScheduler()).thenReturn(scheduler);
        when(server.getPluginManager()).thenReturn(pluginManager);
        when(server.getLogger()).thenReturn(Logger.getLogger("bukkit"));
        setStatic(Bukkit.class, "server", server);
        //setStatic(Version.class, "VERSION", Version.V1_16_5);
        servicesManager = new SimpleServicesManager();
        when(server.getServicesManager()).thenReturn(servicesManager);
        when(server.getOfflinePlayer(any(UUID.class))).thenAnswer(invocation -> {
            OfflinePlayer offlinePlayer = mock(OfflinePlayer.class);
            when(offlinePlayer.getUniqueId()).thenReturn(invocation.getArgument(0));
            return offlinePlayer;
        });
        economy = new TestEconomy();
        servicesManager.register(Economy.class, economy.economy, mock(Plugin.class), ServicePriority.Highest);

    }

    private void setStatic(Class<?> clazz, String name, Object value) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            field.set(null, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void setField(Class<?> clazz, Object in, String name, Object value) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            field.set(in, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public TestEconomy getEconomy() {
        return economy;
    }

    public Main getInstance() {
        return instance;
    }

    public void close() {
        try {
            instance.onDisable();
        } finally {
            try {
                deleteDirectory(dataFolder.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.clearProperty("bauction.db.memory");
        }
    }

    public static void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
                .sorted(Comparator.reverseOrder())
                .forEach(file -> {
                    try {
                        if (file.toFile().exists())
                            Files.delete(file);

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        if (path.toFile().exists())
            Files.delete(path);
    }

    public class TestEconomy {
        private final Economy economy;
        private Map<UUID, Double> balances = new HashMap<>();

        public TestEconomy() {
            economy = mock(Economy.class);
            when(economy.getBalance(any(OfflinePlayer.class))).thenAnswer(invocation -> {
                OfflinePlayer player = invocation.getArgument(0);
                return balances.getOrDefault(player.getUniqueId(), 0D);
            });
            when(economy.withdrawPlayer(any(OfflinePlayer.class), any(Double.class))).thenAnswer(invocation -> {
                OfflinePlayer player = invocation.getArgument(0);
                Double d = invocation.getArgument(1);
                balances.put(player.getUniqueId(), getBalance(player.getUniqueId()) - d);
                return mock(EconomyResponse.class);
            });
            when(economy.depositPlayer(any(OfflinePlayer.class), any(Double.class))).thenAnswer(invocation -> {
                OfflinePlayer player = invocation.getArgument(0);
                Double d = invocation.getArgument(1);
                balances.put(player.getUniqueId(), getBalance(player.getUniqueId()) + d);
                return mock(EconomyResponse.class);
            });
        }

        public double getBalance(UUID uuid) {
            return balances.getOrDefault(uuid, 0D);
        }
    }
}