package org.by1337.bauction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.HomeMenu;
import org.by1337.bauction.util.Category;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.argument.ArgumentStrings;
import org.by1337.blib.command.requires.RequiresPermission;
import org.by1337.bmenu.menu.MenuLoader;

import java.util.*;

public class SearchCmd extends Command<CommandSender> {

    private final MenuLoader menuLoader;
    private final String homeMenuId;
    public SearchCmd(String command, MenuLoader menuLoader, String homeMenuId) {
        super(command);
        this.menuLoader = menuLoader;
        this.homeMenuId = homeMenuId;
        requires(new RequiresPermission<>("bauc.search"));
        requires(sender -> sender instanceof Player);
        argument(new ArgumentStrings<>("tags"));
        executor(this::execute);
    }

    private void execute(CommandSender sender, ArgumentMap<String, Object> args) throws CommandException {
       Player player = (Player) sender;

        String[] rawTags = ((String) args.getOrThrow("tags", Lang.getMessage("tags_required"))).split(" ");
        List<String> tags = new ArrayList<>();
        for (String rawTag : rawTags) {
            tags.addAll(Main.getTrieManager().getTrie().getAllWithPrefix(rawTag));
        }
        Category custom = Main.getCfg().getSorting().getAs("special.search", Category.class);
        custom.setTags(new HashSet<>(tags));

        var menu = menuLoader.getMenu(homeMenuId);
        Objects.requireNonNull(menu, "Menu " + homeMenuId + " not found!");
        var m = menu.create(player, null);
        if (m instanceof HomeMenu homeMenu0){
            homeMenu0.setCustom(custom);
            homeMenu0.getCategories().add(custom);
            Collections.sort(homeMenu0.getCategories());
        }
        m.open();
    }
}