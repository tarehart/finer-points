package com.nodestand.util;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.Node;
import com.nodestand.nodes.NodeInputException;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class BodyParser {


    public static Set<String> getMajorVersions(String bodyText) {

        BodyTextIterator iterator = new BodyTextIterator(bodyText);

        Set<String> links = new HashSet<>();
        while (iterator.hasNext()) {
            links.add(iterator.next().getMajorVersionStableId());
        }

        return links;
    }

    public static String[] validateAndSortLinks(Collection<Node> children, String assertionBodyText, ArgumentNodeRepository repo) throws NodeRulesException {

        if (StringUtils.isEmpty(assertionBodyText)) {
            return new String[0];
        }

        for (Node child: children) {
            if (child.getBody() == null || child.getBody().getMajorVersion() == null) {
                repo.loadWithMajorVersion(child.getId());
            }
        }

        BodyTextIterator iterator = new BodyTextIterator(assertionBodyText);

        //Matcher m = p.matcher(assertionBodyText);

        List<Node> remainingChildren = new ArrayList<>(children);
        List<String> argumentNodeStables = new LinkedList<>();
        Set<String> majorVersionStables = new HashSet<>();

        while (iterator.hasNext()) {
            String id = iterator.next().getMajorVersionStableId();

            if (majorVersionStables.contains(id)) {
                continue;
            }

            List<Node> matches = remainingChildren.stream().filter(n -> n.getBody().getMajorVersion().getStableId().equals(id)).collect(Collectors.toList());

            if (matches.isEmpty()) {
                throw new NodeInputException("Body text contained an unexpected link!");
            }

            if (matches.size() > 1) {
                throw new NodeInputException("Passed multiple children with the same major version!");
            }

            Node child = matches.get(0);

            remainingChildren.remove(child);
            argumentNodeStables.add(child.getStableId());
            majorVersionStables.add(id);
        }

        if (!remainingChildren.isEmpty()) {
            throw new NodeInputException("There were children not represented in the body text!");
        }

        return argumentNodeStables.stream().toArray(String[]::new);
    }
}
