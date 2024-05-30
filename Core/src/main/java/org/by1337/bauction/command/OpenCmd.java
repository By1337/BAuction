package org.by1337.bauction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu2.HomeMenu;
import org.by1337.bauction.util.Category;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.argument.ArgumentPlayer;
import org.by1337.blib.command.argument.ArgumentSetList;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.blib.util.NameKey;
import org.by1337.bmenu.menu.MenuLoader;

import java.util.Objects;

public class OpenCmd extends Command<CommandSender> {
    private final MenuLoader menuLoader;
    private final String menu;

    public OpenCmd(String command, MenuLoader menuLoader, String menu) {
        super(command);
        this.menuLoader = menuLoader;
        this.menu = menu;
        requires(new RequiresPermission<>("bauc.admin.open"));
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

        var menu = menuLoader.getMenu(this.menu);
        Objects.requireNonNull(menu, "Menu " + this.menu + " not found!");
        var m = menu.create(player, null);
        if (m instanceof HomeMenu homeMenu){
            int index = homeMenu.getCategories().indexOf(category);
            if (index == -1) {
                Main.getMessage().sendMsg(sender, "unknown category %s", categoryS);
            }
            homeMenu.getCategories().current = index;
        }
        m.open();
    }
}