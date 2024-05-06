package org.by1337.bauction.event;

import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.blib.chat.Placeholderable;
import org.jetbrains.annotations.NotNull;

public class Event implements Placeholderable {
    private final @NotNull Player player;
    private final @NotNull EventType type;
    private final @NotNull Placeholderable placeholder;

    public Event(@NotNull Player player, @NotNull EventType type, @NotNull Placeholderable placeholder) {
        this.player = player;
        this.placeholder = placeholder;
        this.type = type;
    }

    @Override
    public String replace(String string) {
        return Main.getMessage().setPlaceholders(player, placeholder.replace(string));
    }

    public @NotNull Player getPlayer() {
        return player;
    }

    public @NotNull EventType getType() {
        return type;
    }
}
