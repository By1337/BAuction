package org.by1337.bauction.command.impl;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.db.kernel.User;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.PlayerItemsView;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.CommandSyntaxError;
import org.by1337.blib.command.argument.Argument;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.bmenu.menu.MenuLoader;

import java.util.*;
import java.util.regex.Pattern;

public class ViewCommand extends Command<CommandSender> {
    private final MenuLoader menuLoader;
    private final String openMenu;

    public ViewCommand(String command, MenuLoader menuLoader, String openMenu) {
        super(command);
        this.menuLoader = menuLoader;
        this.openMenu = openMenu;
        requires(new RequiresPermission<>("bauc.view"));
        requires(sender -> sender instanceof Player);
        argument(new ArgumentOfflinePlayerUUID("player"));

        executor(this::execute);

    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
        Player senderP = (Player) sender;

        Object uuid0 = args.get("player");

        if (uuid0 == null) {
            Main.getMessage().sendMsg(sender, Lang.getMessage("player-not-selected"));
            return;
        }

        UUID uuid;
        if (uuid0 instanceof UUID uuid1) {
            uuid = uuid1;
        } else if (ArgumentOfflinePlayerUUID.isUUID(String.valueOf(uuid0))) {
            uuid = UUID.fromString(String.valueOf(uuid0));
        } else {
            Player pl = Bukkit.getPlayerExact(String.valueOf(uuid0));
            if (pl != null) {
                uuid = pl.getUniqueId();
            } else {
                uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + uuid0).getBytes(Charsets.UTF_8));
            }
        }

        User user = Main.getStorage().getUser(uuid);

        if (user == null) {
            Main.getMessage().sendMsg(sender, Lang.getMessage("player-not-found"));
            return;
        }

        var menu = menuLoader.getMenu(openMenu);
        Objects.requireNonNull(menu, "Menu " + openMenu + " not found!");

        var m = menu.create(senderP, null);
        if (m instanceof PlayerItemsView playerItemsView) {
            playerItemsView.setUuid(uuid);
            playerItemsView.setName(user.getNickName());
        }
        m.open();
    }

    private static class ArgumentOfflinePlayerUUID extends Argument<CommandSender> {
        private static final Pattern UUID_REGEX =
                Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

        public ArgumentOfflinePlayerUUID(String name) {
            super(name);
        }

        @Override
        public Object process(CommandSender sender, String str) throws CommandSyntaxError {
            if (str == null || str.isEmpty()) return null;
            if (isUUID(str)) return UUID.fromString(str);
            return str;
//            Player player = Bukkit.getPlayerExact(str);
//            if (player != null) return player.getUniqueId();
//
//            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + str).getBytes(Charsets.UTF_8));
        }

        @Override
        public List<String> tabCompleter(CommandSender sender, String str) throws CommandSyntaxError {
            Player player = sender instanceof Player ? (Player) sender : null;
            var list = new ArrayList<>(Arrays.stream(Bukkit.getOnlinePlayers().stream()
                    .filter(pl -> player == null || player.canSee(pl)).map(Player::getName).toArray(String[]::new)).toList()
            );
            if (str.isEmpty())
                return list;
            return list.stream().filter(s -> s.startsWith(str)).toList();
        }

        public static boolean isUUID(String str) {
            return UUID_REGEX.matcher(str).matches();
        }
    }
}