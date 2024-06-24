package org.by1337.bauction.event;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.by1337.bauction.Main;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.argument.ArgumentEnumValue;
import org.by1337.blib.command.argument.ArgumentFloat;
import org.by1337.blib.command.argument.ArgumentStrings;
import org.by1337.blib.configuration.YamlContext;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EventManager {
    private final Map<String, EventListener> listenerMap = new HashMap<>();
    private final Command<Event> command = new Command<>("");

    public EventManager(YamlContext context) {
        initCommands();
        context.getMap("listeners", YamlContext.class).forEach((k, v) -> {
            var event = EventType.valueOf(v.getAsString("event").toUpperCase(Locale.ENGLISH));
            var commands = v.getList("commands", String.class);
            listenerMap.put(k, new EventListener(event, commands, command));
        });
    }

    public void onEvent(Event event) {
        for (String s : listenerMap.keySet()) {
            EventListener listener = listenerMap.get(s);
            try {
                listener.onEvent(event);
            } catch (Throwable t) {
                Main.getMessage().error("An error occurred while event listener %s was processing the event.", t, s);
            }
        }
    }

    private void initCommands() {
        command.addSubCommand(new Command<Event>("[SOUND]")
                .argument(new ArgumentEnumValue<>("sound", Sound.class))
                .argument(new ArgumentFloat<>("volume"))
                .argument(new ArgumentFloat<>("pitch"))
                .executor((event, args) -> {
                    Sound s = (Sound) args.getOrThrow("sound", "Use: [SOUND] <sound> <?volume> <?pitch>");
                    float volume = (float) args.getOrDefault("volume", 1f);
                    float pitch = (float) args.getOrDefault("pitch", 1f);
                    Main.getMessage().sendSound(event.getPlayer(), s, volume, pitch);
                })).addSubCommand(new Command<Event>("[MESSAGE]")
                .argument(new ArgumentStrings<>("message"))
                .executor((event, args) -> {
                    String msg = (String) args.getOrThrow("message", "Use: [MESSAGE] <message>");
                    Main.getMessage().sendMsg(event.getPlayer(), event.replace(msg));
                })).addSubCommand(new Command<Event>("[CONSOLE]")
                .argument(new ArgumentStrings<>("command"))
                .executor((event, args) -> {
                    String msg = (String) args.getOrThrow("command", "Use: [CONSOLE] <command>");
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), Main.getMessage().messageBuilder(event.replace(msg))));
                })).addSubCommand(new Command<Event>("[PLAYER]")
                .argument(new ArgumentStrings<>("command"))
                .executor((event, args) -> {
                    String msg = (String) args.getOrThrow("command", "Use: [PLAYER] <command>");
                    Bukkit.getScheduler().runTask(Main.getInstance(), () -> event.getPlayer().performCommand(Main.getMessage().messageBuilder(event.replace(msg))));
                }));
    }

}
