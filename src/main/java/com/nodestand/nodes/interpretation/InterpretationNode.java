package com.nodestand.nodes.interpretation;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.Build;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class InterpretationNode extends ArgumentNode {
    private final String type = "interpretation";

    @RelatedTo(type="INTERPRETS", direction = Direction.OUTGOING)
    private SourceNode source;

    public InterpretationNode() {}

    public InterpretationNode(InterpretationBody body, Build build) {
        super(body, build);

    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public ArgumentNode cloneForMinorVersionUpdate(ArgumentNode updatedNode) throws NodeRulesException {
        InterpretationNode copy;
        if (shouldEditInPlace(updatedNode.getBuild())) {
            copy = this;
        } else {
            copy = new InterpretationNode(getBody(), updatedNode.getBuild());
            copy.setPreviousVersion(this);
        }

        if (!getSource().equals(updatedNode.getPreviousVersion())) {
            throw new NodeRulesException("Incorrect behavior during publish. " +
                    "Tried to increment a node that was not actually a consumer. Increment was attempted on " +
                    this + " and the updated node was " + updatedNode);
        }
        copy.setSource((SourceNode) updatedNode);

        return copy;
    }

    public SourceNode getSource() {
        return source;
    }

    public void setSource(SourceNode source) {
        this.source = source;
    }

    public InterpretationBody getBody() {
        return (InterpretationBody) body;
    }
}
