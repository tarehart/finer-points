package com.nodestand.util;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.junit.Assert.*;

public class AliasGeneratorTest {

    @Test
    public void generateAlias() throws Exception {

        AliasGenerator aliasGenerator = new AliasGenerator();

        for (int i = 0; i < 20; i++) {
            String alias = aliasGenerator.generateAlias();
            Assert.assertNotNull(alias);
            Assert.assertTrue(alias.length() > 0);
            Assert.assertEquals(alias.length(), alias.trim().length());
            System.out.println(alias);
        }
    }

}