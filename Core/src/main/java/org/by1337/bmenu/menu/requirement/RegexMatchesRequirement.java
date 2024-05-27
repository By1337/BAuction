package org.by1337.bmenu.menu.requirement;

import org.bukkit.entity.Player;
import org.by1337.blib.chat.Placeholderable;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.nbt.impl.CompoundTag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexMatchesRequirement  implements Requirement {
    private final String input;
    private final String regex;
    private final boolean not;
    private final Pattern pattern;

    public RegexMatchesRequirement(YamlContext context) {
        input = context.getAsString("input");
        regex = context.getAsString("regex");
        not = context.getAsString("type").startsWith("!");
        pattern = Pattern.compile(regex);
    }

    public RegexMatchesRequirement(CompoundTag nbt) {
        input = nbt.getAsString("input");
        regex = nbt.getAsString("regex");
        not = nbt.getAsString("type").startsWith("!");
        pattern = Pattern.compile(regex);
    }

    @Override
    public boolean test(Placeholderable placeholderable, Player clicker) {
        Matcher m =pattern.matcher(placeholderable.replace(input));
        return not ? !m.find() : m.find();
    }


    @Override
    public RequirementType getType() {
        return RequirementType.REGEX_MATCHES_REQUIREMENT;
    }
}
