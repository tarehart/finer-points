package com.nodestand.util;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

public class IdGenerator {

    private static SecureRandom random = new SecureRandom();

    public static String newId() {

        UUID uuid = UUID.randomUUID();
        String base16 = uuid.toString().replaceAll("-", "");
        BigInteger value = new BigInteger(base16, 16);
        return value.toString(36);
    }
}
