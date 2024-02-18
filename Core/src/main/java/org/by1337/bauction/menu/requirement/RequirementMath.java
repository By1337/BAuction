package org.by1337.bauction.menu.requirement;

import org.by1337.blib.chat.Placeholderable;
import org.by1337.bauction.Main;
import org.by1337.bauction.menu.Menu;
import org.by1337.blib.math.MathParser;

import java.text.ParseException;

public class RequirementMath implements IRequirement {
    private final String input;
    private final String output;

    public RequirementMath(String input, String output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public boolean check(Placeholderable holder, Menu menu) {
        String replacesInput = holder.replace(input);
        String replacesOutput = holder.replace(output);

        try {
            String s = MathParser.math(String.format("math[%s]", replacesInput));
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
        return RequirementType.MATH;
    }
}
