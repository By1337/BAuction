package org.by1337.bauction.menu.requirement;

import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.menu.Menu;

public class RequirementStringContains implements IRequirement {
    private final String input;
    private final String input2;
    private final String output;
    private final String id;

    public RequirementStringContains(String input,String input2, String output, String id) {
        this.input2 = input2;
        this.input = input;
        this.output = output;
        this.id = id;
    }

    @Override
    public boolean check(Placeholderable holder, Menu menu) {
        return String.valueOf(holder.replace(input).contains(holder.replace(input2))).equals(holder.replace(output));
    }
    @Override
    public RequirementType getType() {
        return RequirementType.STRING_CONTAINS;
    }
}