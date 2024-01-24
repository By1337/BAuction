package org.by1337.bauction.util;

import junit.framework.TestCase;

import org.junit.Assert;

public class PlaceholderTest extends TestCase {

    public void testReplace() {
        PlaceholderableTest test = new PlaceholderableTest();
        Assert.assertEquals("message 123test, 321123456 bla bla bla 123", test.replace("message {test}test, {test1}{test}{test2} bla bla bla {test}"));
    }

    private class PlaceholderableTest extends Placeholder {
        public PlaceholderableTest() {
            registerPlaceholder("{test}", () -> "123");
            registerPlaceholder("{test1}", () -> "321");
            registerPlaceholder("{test2}", () -> "456");
        }
    }
}