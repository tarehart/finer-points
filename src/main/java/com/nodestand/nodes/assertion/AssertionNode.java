package com.nodestand.nodes.assertion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.service.VersionHelper;
import com.nodestand.util.BodyParser;
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

    // List of child node ids in order of appearance in the body text.
    // Note that these are not the same ids appearing in the text; those are childnode->body->majorversion->stableId.
    // TODO: make this a String[] when this is fixed: https://github.com/neo4j/neo4j-ogm/issues/127
    private String childOrder;

    public AssertionNode() {}

    public AssertionNode(AssertionBody body) {
        super(body);
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

        if (replacement instanceof AssertionNode) {
            AssertionNode assertionReplacement = ((AssertionNode) replacement);
            if (assertionReplacement.getDependentNodes() == null) {
                assertionReplacement.setDependentNodes(new HashSet<>());
            }
            assertionReplacement.getDependentNodes().add(targetNode);
        } else if (replacement instanceof InterpretationNode) {
            InterpretationNode interpReplacement = ((InterpretationNode) replacement);
            if (interpReplacement.getDependentNodes() == null) {
                interpReplacement.setDependentNodes(new HashSet<>());
            }
            interpReplacement.getDependentNodes().add(targetNode);
        } else {
            throw new NodeRulesException("Can only have Assertions and Interpretations as children of an Assertion.");
        }
    }

    public void updateChildOrder(ArgumentNodeRepository repo) throws NodeRulesException {
        String[] links = BodyParser.validateAndSortLinks(getSupportingNodes(), getBody().getBody(), repo);
        childOrder = String.join(",", links);
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
    public AssertionBody createDraftBody(User author, boolean install) throws NodeRulesException {
        AssertionBody freshBody = new AssertionBody(getBody().getTitle(), getBody().getQualifier(), getBody().getBody(), author, getBody().getMajorVersion());
        VersionHelper.decorateDraftBody(freshBody);
        if (install) {
            installBody(freshBody);
        }
        return freshBody;
    }

    @Override
    public AssertionNode createNewDraft(User author, boolean createBodyDraft) throws NodeRulesException {

        AssertionNode copy;

        if (!body.isPublic()) {
            throw new NodeRulesException("Node is already a draft!");
        }

        if (createBodyDraft) {
            AssertionBody freshBody = createDraftBody(author, false);
            copy = new AssertionNode(freshBody);
        } else {
            copy = new AssertionNode(getBody());
        }

        copy.setSupportingNodes(new HashSet<>(getSupportingNodes()));
        copy.setPreviousVersion(this);

        return copy;
    }

    @Override
    public Set<ArgumentNode> getGraphChildren() {
        return supportingNodes != null? supportingNodes : new HashSet<>(0);
    }

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

    public String getChildOrder() {
        return childOrder;
    }
}
