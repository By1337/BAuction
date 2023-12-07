package org.by1337.bauction.menu.command;


import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.by1337.api.command.Command;
import org.by1337.api.command.argument.ArgumentEnumValue;
import org.by1337.api.command.argument.ArgumentStrings;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.AsyncClickListener;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.util.Pair;

import java.util.Objects;

public class CommandUtility {
    private static final Command<Pair<Menu, Player>> command;

    static {
        command = new Command<Pair<Menu, Player>>("[UTIL]")
                .addSubCommand(new Command<Pair<Menu, Player>>("[CONSOLE]")
                        .argument(new ArgumentStrings<>("cmd"))
                        .executor((pair, args) -> {
                                    String cmd = (String) args.getOrThrow("cmd");
                                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), cmd);
                                }
                        )
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[PLAYER]")
                        .argument(new ArgumentStrings<>("cmd"))
                        .executor((sender, args) -> {
                                    String cmd = (String) args.getOrThrow("cmd");
                                    Objects.requireNonNull(sender.getValue(), "player is null!").performCommand(cmd);
                                }
                        )

                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[SOUND]")
                        .argument(new ArgumentEnumValue<>("sound", Sound.class))
                        .executor((sender, args) -> {
                                    Sound sound = (Sound) args.getOrThrow("sound");
                                    Main.getMessage().sendSound(Objects.requireNonNull(sender.getValue(), "player is null!"), sound);
                                }
                        )
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[CLOSE]")
                        .executor((sender, args) -> Objects.requireNonNull(sender.getValue(), "player is null!").closeInventory()
                        )
                )
                .addSubCommand(new Command<Pair<Menu, Player>>("[BACK]")
                        .executor((sender, args) -> AsyncClickListener.syncUtil(() -> Objects.requireNonNull(Objects.requireNonNull(sender.getKey()).getBackMenu()).reopen()))
                )
        ;
    }
}
