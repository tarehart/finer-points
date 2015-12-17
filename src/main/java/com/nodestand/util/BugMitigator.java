package com.nodestand.util;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.assertion.AssertionNode;
import org.neo4j.ogm.session.Session;

public class BugMitigator {

    public static ArgumentNode loadArgumentNode(Session session, Long nodeId, int depth) {
        ArgumentNode node = session.load(ArgumentNode.class, nodeId, depth);

        if (node instanceof AssertionNode) {
            AssertionNode assertionNode = (AssertionNode) node;
            if (assertionNode.getPreviousVersion() != null) {
                // https://jira.spring.io/browse/DATAGRAPH-788
                assertionNode.getSupportingNodes().remove(assertionNode.getPreviousVersion());
            }
        }

        return node;
    }

}
