package com.nodestand.util;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class BodyParser {

    private static Pattern p = Pattern.compile("\\{\\{\\[([0-9a-z]{1,25})\\](.+?)(?=}})\\}\\}"); // {{[9t8io4jpiw7c1y28nq6rxyj1t]Hello world}}

    public static Map<Long, String> getLinks(String bodyText) {

        Matcher m = p.matcher(bodyText);

        Map<Long, String> links = new HashMap<>();
        while (m.find()) {
            links.put(Long.parseLong(m.group(1)), m.group(2));
        }

        return links;
    }


    public static String[] validateAndSortLinks(Collection<ArgumentNode> children, String assertionBodyText, ArgumentNodeRepository repo) throws NodeRulesException {

        if (StringUtils.isEmpty(assertionBodyText)) {
            return new String[0];
        }

        for (ArgumentNode child: children) {
            if (child.getBody() == null || child.getBody().getMajorVersion() == null) {
                repo.loadWithMajorVersion(child.getId());
            }
        }

        Matcher m = p.matcher(assertionBodyText);

        List<ArgumentNode> remainingChildren = new ArrayList<>(children);
        List<String> argumentNodeStables = new LinkedList<>();
        Set<String> majorVersionStables = new HashSet<>();

        while (m.find()) {
            String id = m.group(1);

            if (majorVersionStables.contains(id)) {
                continue;
            }

            List<ArgumentNode> matches = remainingChildren.stream().filter(n -> n.getBody().getMajorVersion().getStableId().equals(id)).collect(Collectors.toList());

            if (matches.isEmpty()) {
                throw new NodeRulesException("Body text contained an unexpected link!");
            }

            if (matches.size() > 1) {
                throw new NodeRulesException("Passed multiple children with the same major version!");
            }

            ArgumentNode child = matches.get(0);

            remainingChildren.remove(child);
            argumentNodeStables.add(child.getStableId());
            majorVersionStables.add(id);
        }

        if (!remainingChildren.isEmpty()) {
            throw new NodeRulesException("There were children not represented in the body text!");
        }

        return argumentNodeStables.stream().toArray(String[]::new);
    }
}
