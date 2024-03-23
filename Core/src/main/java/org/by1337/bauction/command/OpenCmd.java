package org.by1337.bauction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.lang.Lang;
//import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.util.Category;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.argument.ArgumentPlayer;
import org.by1337.blib.command.argument.ArgumentSetList;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.util.NameKey;

public class OpenCmd extends Command<CommandSender> {

    public OpenCmd(String command) {
        super(command);
        requires(new RequiresPermission<>("bauc.admin.create"));
        argument(new ArgumentPlayer<>("player"));
        argument(new ArgumentSetList<>("category", Main.getCfg().getCategoryMap().keySet().stream().map(NameKey::getName).toList()));
        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
        Player player = (Player) args.getOrThrow("player");
        String categoryS = (String) args.getOrThrow("category");

        Category category = Main.getCfg().getCategoryMap().get(new NameKey(categoryS, true));

        if (category == null) {
            Main.getMessage().sendMsg(sender, "unknown category %s", categoryS);
            return;
        }

//        User user = Main.getStorage().getUserOrCreate(player);
//        MainMenu menu = new MainMenu(user, player);
//
//        int index = menu.getCategories().indexOf(category);
//
//        if (index == -1) {
//            Main.getMessage().sendMsg(sender, "unknown category %s", categoryS);
//            menu.close();
//            return;
//        }
//        menu.getCategories().current = index;
//        menu.create();
    }
}