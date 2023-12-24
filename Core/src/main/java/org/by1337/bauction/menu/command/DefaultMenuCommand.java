package org.by1337.bauction.menu.command;


import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.by1337.api.command.Command;
import org.by1337.api.command.CommandException;
import org.by1337.api.command.argument.ArgumentEnumValue;
import org.by1337.api.command.argument.ArgumentSetList;
import org.by1337.api.command.argument.ArgumentString;
import org.by1337.api.command.argument.ArgumentStrings;
import org.by1337.bauction.Main;
import org.by1337.bauction.action.BuyItemProcess;
import org.by1337.bauction.auc.SellItem;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.AsyncClickListener;
import org.by1337.bauction.menu.Menu;
import org.by1337.bauction.menu.impl.ItemsForSaleMenu;
import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.menu.impl.UnsoldItemsMenu;
import org.by1337.bauction.util.CUniqueName;
import org.by1337.api.util.Pair;
import org.by1337.bauction.util.UniqueName;

import java.util.List;
import java.util.Objects;

public class DefaultMenuCommand {
    public static final Command<Pair<Menu, Player>> command;

    static {
        command = new Command<Pair<Menu, Player>>("cmds")
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
                .addSubCommand(new Command<Pair<Menu, Player>>("[OPEN_MENU]")
                        .argument(new ArgumentString<>("menu"))
                        .executor(((sender, args) -> {
                            String menuId = (String) args.getOrThrow("menu");
                            if (menuId.equals("selling-items")) {
                                ItemsForSaleMenu items = new ItemsForSaleMenu(sender.getValue(), sender.getKey().getUser(), sender.getKey());
                                items.open();
                            } else if (menuId.equals("unsold-items")) {
                                UnsoldItemsMenu unsoldItemsMenu = new UnsoldItemsMenu(sender.getValue(), sender.getKey().getUser(), sender.getKey());
                                unsoldItemsMenu.open();
                            } else if (menuId.equals("main-menu")) {
                                MainMenu menu = new MainMenu(sender.getKey().getUser(), sender.getValue(), sender.getKey());
                                menu.open();
                            } else {
                                throw new CommandException("unknown menu id: " + menuId);
                            }
                        }))
                )
        ;
    }

}
