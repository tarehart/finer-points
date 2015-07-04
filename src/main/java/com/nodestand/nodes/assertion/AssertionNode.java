package com.nodestand.nodes.assertion;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.Build;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import java.util.HashSet;
import java.util.Set;


@NodeEntity
public class AssertionNode extends ArgumentNode {

    private static final String TYPE = "assertion";

    @RelatedTo(type="SUPPORTED_BY", direction = Direction.OUTGOING)
    private Set<ArgumentNode> supportingNodes;

    public AssertionNode() {}

    public AssertionNode(AssertionBody body, Build build) {
        super(body, build);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public Set<ArgumentNode> getSupportingNodes() {
        return supportingNodes;
    }

    public void setSupportingNodes(Set<ArgumentNode> nodes) {
        supportingNodes = nodes;
    }

    public void supportedBy(ArgumentNode a) {

        assert !(a instanceof SourceNode);

        if (supportingNodes == null) {
            supportingNodes = new HashSet<>();
        }
        supportingNodes.add(a);
    }
}
