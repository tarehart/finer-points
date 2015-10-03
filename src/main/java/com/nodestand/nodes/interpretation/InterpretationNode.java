package com.nodestand.nodes.interpretation;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.Build;
import com.nodestand.nodes.version.VersionHelper;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class InterpretationNode extends ArgumentNode {
    private final String type = "interpretation";

    @Relationship(type="INTERPRETS", direction = Relationship.OUTGOING)
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
    public ArgumentNode alterOrCloneToPointToChild(ArgumentNode updatedChildNode) throws NodeRulesException {
        InterpretationNode copy;
        if (shouldEditInPlace(updatedChildNode.getBuild())) {
            copy = this;
        } else {
            copy = createNewDraft(updatedChildNode.getBuild(), false);
        }

        if (!getSource().getId().equals(updatedChildNode.getPreviousVersion().getId())) {
            throw new NodeRulesException("Incorrect behavior during publish. " +
                    "Tried to increment a node that was not actually a consumer. Increment was attempted on " +
                    this + " and the updated node was " + updatedChildNode);
        }
        copy.setSource((SourceNode) updatedChildNode);

        return copy;
    }

    @Override
    public InterpretationNode createNewDraft(Build build, boolean createBodyDraft) throws NodeRulesException {
        InterpretationNode copy;

        if (isDraft()) {
            throw new NodeRulesException("Node is already a draft!");
        }

        if (createBodyDraft) {
            InterpretationBody freshBody = new InterpretationBody(getBody().getTitle(), getBody().getBody(), build.author, getBody().getMajorVersion());
            VersionHelper.decorateDraftBody(freshBody);
            copy = new InterpretationNode(freshBody, build);
        } else {
            copy = new InterpretationNode(getBody(), build);
        }

        copy.setSource(this.getSource());
        copy.setPreviousVersion(this);

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
