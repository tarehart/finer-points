package com.nodestand.nodes.assertion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.Build;
import com.nodestand.service.VersionHelper;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;


@NodeEntity
public class AssertionNode extends ArgumentNode {

    private final String type = "assertion";

    @Relationship(type="SUPPORTED_BY", direction = Relationship.OUTGOING)
    private Set<ArgumentNode> supportingNodes;

    @Relationship(type="SUPPORTED_BY", direction = Relationship.INCOMING)
    private Set<AssertionNode> dependentNodes;

    public AssertionNode() {}

    public AssertionNode(AssertionBody body, Build build) {
        super(body, build);
    }

    @Override
    public String getType() {
        return type;
    }


    private static void performChildReplacement(ArgumentNode replacement, ArgumentNode existing, AssertionNode targetNode) throws NodeRulesException {
        // Make sure we no longer depend on the previous version
        if (!targetNode.getSupportingNodes().removeIf(n -> n.getId().equals(existing.getId()))) {
            throw new NodeRulesException("Incorrect behavior when updating to point to new child. " +
                    "Tried to update a node that was not actually a consumer. Attempt was made on " +
                    targetNode + " trying to replace " + existing + " with " + replacement);
        }

        // Make sure the previous version no longer claims this as a dependent
        existing.getDependentNodes().removeIf(n -> n.getId().equals(targetNode.getId()));

        targetNode.getSupportingNodes().add(replacement);
    }

    @Override
    public void alterToPointToChild(ArgumentNode replacementChild, ArgumentNode existing) throws NodeRulesException {
        if (!shouldEditInPlace()) {
            throw new NodeRulesException("Called alterToPointToChild on a node that should not be edited in place!");
        }
        performChildReplacement(replacementChild, existing, this);
    }

    @Override
    public void copyContentTo(ArgumentNode target) throws NodeRulesException {
        AssertionNode assertionTarget = (AssertionNode) target;
        assertionTarget.setSupportingNodes(supportingNodes);

        for (ArgumentNode supportingNode : supportingNodes) {
            if (supportingNode instanceof AssertionNode) {
                ((AssertionNode) supportingNode).getDependentNodes().add(assertionTarget);
            } else if (supportingNode instanceof InterpretationNode) {
                ((InterpretationNode) supportingNode).getDependentNodes().add(assertionTarget);
            }
        }
    }

    @Override
    public ArgumentNode alterOrCloneToPointToChild(ArgumentNode updatedChildNode, ArgumentNode existing) throws NodeRulesException {

        AssertionNode copy;

        if (shouldEditInPlace()) {
            copy = this;
        } else {
            copy = createNewDraft(updatedChildNode.getBuild(), true);
        }

        performChildReplacement(updatedChildNode, existing, copy);

        return copy;
    }

    @Override
    public AssertionBody createDraftBody(User author, boolean install) throws NodeRulesException {
        AssertionBody freshBody = new AssertionBody(getBody().getTitle(), getBody().getBody(), author, getBody().getMajorVersion());
        VersionHelper.decorateDraftBody(freshBody);
        if (install) {
            installBody(freshBody);
        }
        return freshBody;
    }

    @Override
    public AssertionNode createNewDraft(Build build, boolean createBodyDraft) throws NodeRulesException {

        AssertionNode copy;

        if (!body.isPublic()) {
            throw new NodeRulesException("Node is already a draft!");
        }

        if (createBodyDraft) {
            AssertionBody freshBody = createDraftBody(build.author, false);
            copy = new AssertionNode(freshBody, build);
        } else {
            copy = new AssertionNode(getBody(), build);
        }

        copy.setSupportingNodes(new HashSet<>(getSupportingNodes()));
        copy.setPreviousVersion(this);

        return copy;
    }

    @Override
    public Set<ArgumentNode> getGraphChildren() {
        return supportingNodes != null? supportingNodes : new HashSet<>(0);
    }

//    @Override
//    public Set<ArgumentNode> getDependentNodesGeneric() {
//        return new HashSet<>(getDependentNodes());
//    }

    @JsonIgnore
    @Relationship(type="SUPPORTED_BY", direction = Relationship.OUTGOING)
    public Set<ArgumentNode> getSupportingNodes() {
        return supportingNodes;
    }

    @Relationship(type="SUPPORTED_BY", direction = Relationship.OUTGOING)
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
