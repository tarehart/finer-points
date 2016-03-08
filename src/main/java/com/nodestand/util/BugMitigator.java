package com.nodestand.util;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.assertion.AssertionNode;
import org.neo4j.ogm.session.Session;
import org.springframework.data.neo4j.template.Neo4jOperations;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BugMitigator {

    public static ArgumentNode loadArgumentNode(Neo4jOperations operations, Long nodeId, int depth) {
        ArgumentNode node = operations.load(ArgumentNode.class, nodeId, depth);

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
