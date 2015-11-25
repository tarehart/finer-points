package com.nodestand.nodes.assertion;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.Build;
import com.nodestand.nodes.version.VersionHelper;
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



    @Override
    public ArgumentNode alterOrCloneToPointToChild(ArgumentNode updatedChildNode) throws NodeRulesException {

        AssertionNode copy;

        if (shouldEditInPlace(updatedChildNode.getBuild())) {
            copy = this;
        } else {
            copy = createNewDraft(updatedChildNode.getBuild(), false);
        }

        // Make sure we no longer depend on the previous version
        if (!copy.getSupportingNodes().removeIf(n -> n.getId().equals(updatedChildNode.getPreviousVersion().getId()))) {
            throw new NodeRulesException("Incorrect behavior when updating to point to new child. " +
                    "Tried to increment a node that was not actually a consumer. Increment was attempted on " +
                    this + " and the updated node was " + updatedChildNode);
        }

        // Make sure the previous version no longer claims this as a dependent
        updatedChildNode.getPreviousVersion().getDependentNodes().removeIf(n -> n.getId().equals(getId()));

        copy.getSupportingNodes().add(updatedChildNode);
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

        if (isDraft()) {
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
