package org.by1337.bauction.util;

import junit.framework.TestCase;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.configuration.file.YamlConfiguration;
import org.by1337.bauction.menu.requirement.StringEqualsContainsRequirement;
import org.junit.Assert;

public class OptionParserTest extends TestCase {

    public void testParse() {
//        String input = "-flag value -superFlag superValue -str simple message";
//        OptionParser parser = new OptionParser();
//        parser.parse(input);
//
//        Assert.assertEquals(parser.get("flag"), "value");
//        Assert.assertEquals(parser.get("superFlag"), "superValue");
//        Assert.assertEquals(parser.get("str"), "simple message");


        StringEqualsContainsRequirement requirement = new StringEqualsContainsRequirement("{id}", "{id", false);

        System.out.println(
                ((YamlConfiguration)requirement.saveAsYaml().getHandle()).saveToString()
        );


       // System.out.println(requirement.saveAsNbt().toStringBeautifier());
    }
}