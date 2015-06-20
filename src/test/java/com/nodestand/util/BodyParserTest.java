package com.nodestand.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class BodyParserTest {

    @Test
    public void testParser() {

        String input = "Hello, {{[34]this}} text has some {links}} embedded {{[2]in it}}.";

        Map<Long, String> expectedMap = new HashMap<>();
        expectedMap.put(34L, "this");
        expectedMap.put(2L, "in it");

        Map<Long, String> actualMap = BodyParser.getLinks(input);

        Assert.assertEquals(expectedMap, actualMap);
    }

}
