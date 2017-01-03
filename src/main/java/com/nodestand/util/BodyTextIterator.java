package com.nodestand.util;

import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Tyler on 1/1/2017.
 */
public class BodyTextIterator implements Iterator<BodyTextIterator.BodyLink> {

    private static Pattern p = Pattern.compile("\\{\\{\\[([0-9a-z]{1,25})\\](.+?)(?=}})\\}\\}"); // {{[9t8io4jpiw7c1y28nq6rxyj1t]Hello world}}

    private final Matcher matcher;
    private BodyLink next;

    public BodyTextIterator(String assertionBodyText) {
        matcher = p.matcher(assertionBodyText);
        loadNext();
    }

    private void loadNext() {
        if (matcher.find()) {
            next = new BodyLink();
            next.majorVersionStableId = matcher.group(1);
            next.label = matcher.group(2);
        } else {
            next = null;
        }
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public BodyLink next() {
        BodyLink current = next;
        loadNext();
        return current;
    }

    public static class BodyLink {
        private String majorVersionStableId;
        private String label;

        public String getMajorVersionStableId() {
            return majorVersionStableId;
        }

        public String getLabel() {
            return label;
        }
    }
}
