package com.nodestand.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BodyParser {

    public static Map<Long, String> getLinks(String bodyText) {
        Pattern p = Pattern.compile("\\{\\{\\[([0-9]+)\\](.+?)(?=}})\\}\\}"); // {{[3]Hello world}}

        Matcher m = p.matcher(bodyText);

        Map<Long, String> links = new HashMap<>();
        while (m.find()) {
            links.put(Long.parseLong(m.group(1)), m.group(2));
        }

        return links;
    }
}
