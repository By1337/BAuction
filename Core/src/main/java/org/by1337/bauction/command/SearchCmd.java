package org.by1337.bauction.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.by1337.bauction.Main;
import org.by1337.bauction.api.auc.User;
import org.by1337.bauction.lang.Lang;
import org.by1337.bauction.menu.impl.MainMenu;
import org.by1337.bauction.util.Category;
import org.by1337.bauction.util.TimeCounter;
import org.by1337.blib.command.Command;
import org.by1337.blib.command.CommandException;
import org.by1337.blib.command.CommandSyntaxError;
import org.by1337.blib.command.argument.ArgumentMap;
import org.by1337.blib.command.argument.ArgumentStrings;
import org.by1337.blib.command.requires.RequiresPermission;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SearchCmd extends Command<CommandSender> {

    public SearchCmd(String command) {
        super(command);
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

        User user = Main.getStorage().getUserOrCreate(player);

        MainMenu menu = new MainMenu(user, player);
        menu.setCustomCategory(custom);
        menu.open();
    }
}