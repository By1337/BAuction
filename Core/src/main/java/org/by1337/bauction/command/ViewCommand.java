package org.by1337.bauction.command;

import com.google.common.base.Charsets;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.impl.PlayerItemsView;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.CommandSyntaxError;
import org.by1337.blib.command.argument.Argument;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.requires.RequiresPermission;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class ViewCommand extends Command<CommandSender> {

    public ViewCommand(String command) {
        super(command);
        requires(new RequiresPermission<>("bauc.view"));
        requires(sender -> sender instanceof Player);
        argument(new ArgumentOfflinePlayerUUID("player"));

        executor(this::execute);

    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
        Player senderP = (Player) sender;

        UUID uuid = (UUID) args.get("player");
        if (uuid == null) {
            Main.getMessage().sendMsg(sender, Lang.getMessage("player-not-selected"));
            return;
        }

        if (!Main.getStorage().hasUser(uuid)) {
            Main.getMessage().sendMsg(sender, Lang.getMessage("player-not-found"));
            return;
        }

        User user = Main.getStorage().getUserOrCreate(senderP);
        PlayerItemsView menu = new PlayerItemsView(user, senderP, uuid, Bukkit.getOfflinePlayer(uuid).getName());
        menu.open();
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

            Player player = Bukkit.getPlayerExact(str);
            if (player != null) return player.getUniqueId();

            return UUID.nameUUIDFromBytes(("OfflinePlayer:" + str).getBytes(Charsets.UTF_8));
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
        public static boolean isUUID(String str){
            return UUID_REGEX.matcher(str).matches();
        }
    }
}