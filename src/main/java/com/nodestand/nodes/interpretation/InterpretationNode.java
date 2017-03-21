package com.nodestand.nodes.interpretation;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.*;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.source.SourceNode;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

@NodeEntity
public class InterpretationNode extends ArgumentNode {

    @Relationship(type="INTERPRETS", direction = Relationship.OUTGOING)
    private LeafNode leafNode;

    @Relationship(type="SUPPORTED_BY", direction = Relationship.INCOMING)
    private Set<AssertionNode> dependentNodes;

    public InterpretationNode() {}

    public InterpretationNode(InterpretationBody body) {
        super(body);

    }

    @Override
    public String getType() {
        return "interpretation";
    }

    @Override
    public void alterToPointToChild(Node updatedChildNode, Node existingChildNode) throws NodeRulesException {

        if (!this.getLeafNode().getStableId().equals(existingChildNode.getStableId())) {
            throw new NodeRulesException("Incorrect behavior while performing child replacement. " +
                    "The caller thought that " + this + " had " + existingChildNode + " as a child, but the current child is actually " + this.getLeafNode());
        }

        this.setLeafNode((SourceNode) updatedChildNode);

        // Make sure the old leafNode no longer claims this as a dependent.
        existingChildNode.getDependentNodes().removeIf(n -> n.getId().equals(this.getId()));
    }

    private InterpretationBody createDraftBody(Author author) throws NodeRulesException {
        InterpretationBody freshBody = new InterpretationBody(getBody().getTitle(), getBody().getQualifier(), getBody().getBody(), author, getBody().getMajorVersion());
        setupDraftBody(freshBody);
        return freshBody;
    }

    @Override
    public InterpretationNode createNewDraft(Author author) throws NodeRulesException {

        if (!body.isPublic()) {
            throw new NodeRulesException("Node is already a draft!");
        }

        InterpretationBody freshBody = createDraftBody(author);

        InterpretationNode copy = new InterpretationNode(freshBody);
        copy.setLeafNode(this.getLeafNode());
        copy.setPreviousVersion(this);

        return copy;
    }

    @Override
    public void copyContentTo(Node target) {
        InterpretationNode interpretationTarget = (InterpretationNode) target;
        interpretationTarget.setLeafNode(leafNode);
        leafNode.getDependentNodes().add(interpretationTarget);
    }

    @Override
    public Set<Node> getGraphChildren() {
        Set<Node> children = new HashSet<>(1);
        if (leafNode != null) {
            children.add(leafNode);
        }
        return children;
    }

    @JsonIgnore
    public LeafNode getLeafNode() {
        return leafNode;
    }

    public void setLeafNode(LeafNode leafNode) {
        this.leafNode = leafNode;
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
