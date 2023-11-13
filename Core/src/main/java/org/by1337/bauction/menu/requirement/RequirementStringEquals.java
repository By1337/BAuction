package org.by1337.bauction.menu.requirement;

import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.menu.Menu;

public class RequirementStringEquals implements IRequirement {
    private final String input;
    private final String output;

    public RequirementStringEquals(String input, String output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean check(Placeholderable holder, Menu menu) {
        String replacesInput = holder.replace(input);
        String replacesOutput = holder.replace(output);

        if ((replacesInput.equals("1") || replacesInput.equals("0")) && (replacesOutput.equals("true") || replacesOutput.equals("false"))) {
            return parseBoolean(replacesInput) == parseBoolean(replacesOutput);
        }
        return replacesInput.equals(replacesOutput);
    }

    @Override
    public RequirementType getType() {
        return RequirementType.STRING_EQUALS;
    }

    public static boolean parseBoolean(String s) {
        if (s.equals("1")) return true;
        return s.equals("true");
    }
}