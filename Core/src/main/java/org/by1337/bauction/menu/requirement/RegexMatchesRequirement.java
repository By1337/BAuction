package org.by1337.bauction.menu.requirement;

import org.bukkit.configuration.file.YamlConfiguration;
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

    public CompoundTag saveAsNbt() {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.putString("input", input);
        compoundTag.putString("regex", regex);
        compoundTag.putString("type", not ? RequirementType.REGEX_MATCHES_REQUIREMENT.notId : RequirementType.REGEX_MATCHES_REQUIREMENT.id);
        return compoundTag;
    }

    public YamlContext saveAsYaml() {
        YamlContext yamlContext = new YamlContext(new YamlConfiguration());
        yamlContext.set("input", input);
        yamlContext.set("regex", regex);
        yamlContext.set("type", not ? RequirementType.REGEX_MATCHES_REQUIREMENT.notId : RequirementType.REGEX_MATCHES_REQUIREMENT.id);
        return yamlContext;
    }

    @Override
    public RequirementType getType() {
        return RequirementType.REGEX_MATCHES_REQUIREMENT;
    }
}
