package com.nodestand.nodes.interpretation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.service.VersionHelper;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class InterpretationNode extends ArgumentNode {
    private final String type = "interpretation";

    @Relationship(type="INTERPRETS", direction = Relationship.OUTGOING)
    private SourceNode source;

    @Relationship(type="SUPPORTED_BY", direction = Relationship.INCOMING)
    private Set<AssertionNode> dependentNodes;

    public InterpretationNode() {}

    public InterpretationNode(InterpretationBody body) {
        super(body);

    }

    @Override
    public String getType() {
        return type;
    }

    private static void performChildReplacement(ArgumentNode updatedChildNode, ArgumentNode existing, InterpretationNode targetNode) throws NodeRulesException {

        if (!targetNode.getSource().getId().equals(existing.getId())) {
            throw new NodeRulesException("Incorrect behavior while performing child replacement. " +
                    "Tried to swap out " + existing + " as the source on " + targetNode);
        }

        targetNode.setSource((SourceNode) updatedChildNode);

        // Make sure the old source no longer claims this as a dependent.
        existing.getDependentNodes().removeIf(n -> n.getId().equals(targetNode.getId()));
    }

    @Override
    public void alterToPointToChild(ArgumentNode updatedChildNode, ArgumentNode existing) throws NodeRulesException {
        performChildReplacement(updatedChildNode, existing, this);
    }

    @Override
    public InterpretationBody createDraftBody(User author, boolean install) throws NodeRulesException {
        InterpretationBody freshBody = new InterpretationBody(getBody().getTitle(), getBody().getQualifier(), getBody().getBody(), author, getBody().getMajorVersion());
        VersionHelper.decorateDraftBody(freshBody);
        if (install) {
            installBody(freshBody);
        }
        return freshBody;
    }

    @Override
    public InterpretationNode createNewDraft(User author, boolean createBodyDraft) throws NodeRulesException {
        InterpretationNode copy;

        if (!body.isPublic()) {
            throw new NodeRulesException("Node is already a draft!");
        }

        if (createBodyDraft) {
            InterpretationBody freshBody = createDraftBody(author, false);
            copy = new InterpretationNode(freshBody);
        } else {
            copy = new InterpretationNode(getBody());
        }

        copy.setSource(this.getSource());
        copy.setPreviousVersion(this);

        return copy;
    }

    @Override
    public void copyContentTo(ArgumentNode target) {
        InterpretationNode interpretationTarget = (InterpretationNode) target;
        interpretationTarget.setSource(source);
        source.getDependentNodes().add(interpretationTarget);
    }

    @Override
    public Set<ArgumentNode> getGraphChildren() {
        Set<ArgumentNode> children = new HashSet<>(1);
        if (source != null) {
            children.add(source);
        }
        return children;
    }

    @JsonIgnore
    public SourceNode getSource() {
        return source;
    }

    public void setSource(SourceNode source) {
        this.source = source;
    }

    public InterpretationBody getBody() {
        return (InterpretationBody) body;
    }

    @Override
    @JsonIgnore
    @Relationship(type="SUPPORTED_BY", direction = Relationship.INCOMING)
    public Set<AssertionNode> getDependentNodes() {
        return dependentNodes;
    }

    /**
     * Omissions are OK, false positives are not. It's mostly here to be used by the object graph mapper and to mitigate this issue:
     * https://github.com/neo4j/neo4j-ogm/issues/38
     */
    @Relationship(type="SUPPORTED_BY", direction = Relationship.INCOMING)
    public void setDependentNodes(Set<AssertionNode> dependentNodes) {
        this.dependentNodes = dependentNodes;
    }
}
