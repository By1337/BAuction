package org.by1337.bauction.command.argument;

import org.bukkit.command.CommandSender;
import org.by1337.api.command.CommandSyntaxError;
import org.by1337.api.command.argument.Argument;
import org.by1337.api.command.argument.ArgumentInteger;
import org.by1337.api.command.argument.ArgumentIntegerAllowedMatch;
import org.by1337.api.command.argument.ArgumentSetList;
import org.by1337.bauction.Main;

import java.util.List;

public class ArgumentFullOrCount extends Argument<CommandSender> {

    private final ArgumentInteger<CommandSender> argumentInteger = new ArgumentIntegerAllowedMatch<>("amount", 1, 64);


    public ArgumentFullOrCount(String name) {
        super(name);
    }

    @Override
    public List<String> tabCompleter(CommandSender sender, String str) throws CommandSyntaxError {
        if (Main.getCfg().isAllowBuyCount()) {
            if (str.isEmpty()) return List.of("full", "64");
            if (str.startsWith("f")) return List.of("full");
        } else if (str.isEmpty()) return List.of("64");
        return argumentInteger.tabCompleter(sender, str);
    }

    @Override
    public Object process(CommandSender sender, String s) throws CommandSyntaxError {
        if (Main.getCfg().isAllowBuyCount())
            if (s.startsWith("f")) return "full";
        return argumentInteger.process(sender, s);
    }
}
