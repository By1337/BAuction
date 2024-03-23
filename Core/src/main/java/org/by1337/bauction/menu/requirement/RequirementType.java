package org.by1337.bauction.menu.requirement;

import org.by1337.bauction.util.ImmutableArrayList;
import org.by1337.blib.configuration.YamlContext;
import org.by1337.blib.nbt.impl.CompoundTag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public enum RequirementType {
    MATH("math", "!math", MathRequirement::new, MathRequirement::new, List.of("m", "!m")),
    STRING_EQUALS("string equals", "!string equals", StringEqualsRequirement::new, StringEqualsRequirement::new, List.of("se", "!se")),
    STRING_EQUALS_IGNORE_CASE("string equals ignorecase", "!string equals ignorecase", StringEqualsIgnoreCaseRequirement::new, StringEqualsIgnoreCaseRequirement::new, List.of("sei", "!sei")),
    STRING_CONTAINS("string contains", "!string contains", StringEqualsContainsRequirement::new, StringEqualsContainsRequirement::new, List.of("sc", "!sc")),
    REGEX_MATCHES_REQUIREMENT("regex matches", "!regex matches", RegexMatchesRequirement::new, RegexMatchesRequirement::new, List.of("rm", "!rm")),
    HAS_PERMISSION("has permission", "!has permission", HasPermisionRequirement::new, HasPermisionRequirement::new, List.of("hp", "!hp")),
    ;
    public final String id;
    public final String notId;
    public final Function<YamlContext, Requirement> fromYaml;
    public final Function<CompoundTag, Requirement> fromNbt;
    public final ImmutableArrayList<String> aliases;

    RequirementType(String id, String notId, Function<YamlContext, Requirement> fromYaml, Function<CompoundTag, Requirement> fromNbt) {
        this.id = id;
        this.notId = notId;
        this.fromYaml = fromYaml;
        this.fromNbt = fromNbt;
        aliases = new ImmutableArrayList<>(new ArrayList<>());
    }

    RequirementType(String id, String notId, Function<YamlContext, Requirement> fromYaml, Function<CompoundTag, Requirement> fromNbt, List<String> aliases) {
        this.id = id;
        this.notId = notId;
        this.fromYaml = fromYaml;
        this.fromNbt = fromNbt;
        this.aliases = new ImmutableArrayList<>(aliases);
    }

    @Nullable
    public static RequirementType byName(String name) {
        String id;
        if (name.charAt(1) == '!') {
            id = name.substring(1);
        } else {
            id = name;
        }
        for (RequirementType type : values()) {
            if (type.id.equals(id) || type.aliases.contains(id)){
                return type;
            }
        }
        return null;
    }
}
