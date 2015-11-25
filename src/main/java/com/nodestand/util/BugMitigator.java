package com.nodestand.util;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.assertion.AssertionNode;
import org.springframework.data.neo4j.template.Neo4jOperations;

public class BugMitigator {

    public static ArgumentNode loadArgumentNode(Neo4jOperations ops, Long nodeId, int depth) {
        ArgumentNode node = ops.load(ArgumentNode.class, nodeId, depth);

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
