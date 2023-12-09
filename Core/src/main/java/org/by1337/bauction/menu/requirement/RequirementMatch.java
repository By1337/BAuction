package org.by1337.bauction.menu.requirement;

import org.by1337.api.chat.Placeholderable;
import org.by1337.api.match.BMatch;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.Menu;

import java.text.ParseException;

public class RequirementMatch implements IRequirement {
    private final String input;
    private final String output;

    public RequirementMatch(String input, String output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean check(Placeholderable holder, Menu menu) {
        String replacesInput = holder.replace(input);
        String replacesOutput = holder.replace(output);

        try {
            String s = BMatch.match(String.format("match[%s]", replacesInput));
            if (s.equals("1")) return replacesOutput.equals("true");
            if (s.equals("0")) return replacesOutput.equals("false");
            return s.equals(replacesOutput);
        } catch (ParseException e) {
            Main.getMessage().error(e);
            return false;
        }
    }

    @Override
    public RequirementType getType() {
        return RequirementType.MATCH;
    }
}
