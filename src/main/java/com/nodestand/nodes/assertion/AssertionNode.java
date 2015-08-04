package com.nodestand.nodes.assertion;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.Build;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;


@NodeEntity
public class AssertionNode extends ArgumentNode {

    private final String type = "assertion";

    @RelatedTo(type="SUPPORTED_BY", direction = Direction.OUTGOING)
    private Set<ArgumentNode> supportingNodes;

    public AssertionNode() {}

    public AssertionNode(AssertionBody body, Build build) {
        super(body, build);
    }

    @Override
    public String getType() {
        return type;
    }



    @Override
    public ArgumentNode cloneForMinorVersionUpdate(ArgumentNode updatedNode) throws NodeRulesException {

        AssertionNode copy;

        if (shouldEditInPlace(updatedNode.getBuild())) {
            copy = this;
        } else {
            copy = new AssertionNode(getBody(), updatedNode.getBuild());
            copy.setSupportingNodes(new TreeSet<>(getSupportingNodes()));
            copy.setPreviousVersion(this);
        }

        if (!copy.getSupportingNodes().remove(updatedNode.getPreviousVersion())) {
            throw new NodeRulesException("Incorrect behavior during publish. " +
                    "Tried to increment a node that was not actually a consumer. Increment was attempted on " +
                    this + " and the updated node was " + updatedNode);
        }
        copy.getSupportingNodes().add(updatedNode);
        return copy;
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

    public AssertionBody getBody() {
        return (AssertionBody) body;
    }
}
