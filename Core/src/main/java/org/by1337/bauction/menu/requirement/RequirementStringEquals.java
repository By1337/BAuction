package org.by1337.bauction.menu.requirement;

import org.by1337.api.chat.Placeholderable;
import org.by1337.bauction.menu.Menu;

public class RequirementStringEquals implements IRequirement {
    private final String input;
    private final String input1;
    private final String output;

    public RequirementStringEquals(String input, String input1, String output) {
        this.input = input;
        this.input1 = input1;
        this.output = output;
    }

    @Override
    public boolean check(Placeholderable holder, Menu menu) {
        String replacesInput = holder.replace(input);
        String replacesInput1 = holder.replace(input1);

        return String.valueOf(replacesInput.equals(replacesInput1)).equals(holder.replace(output));
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