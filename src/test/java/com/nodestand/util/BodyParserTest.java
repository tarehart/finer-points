package com.nodestand.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BodyParserTest {

    @Test
    public void testParser() {

        String input = "Hello, {{[34ab]this}} text has some {links}} embedded {{[2cd]in it}}.";

        Set<String> expectedSet = new HashSet<>();
        expectedSet.add("34ab");
        expectedSet.add("2cd");

        Set<String> actualSet = BodyParser.getMajorVersions(input);

        Assert.assertEquals(expectedSet, actualSet);
    }

}
