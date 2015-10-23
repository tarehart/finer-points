package com.nodestand.nodes.assertion;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
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



        if (!copy.getSupportingNodes().removeIf(n -> n.getId() == updatedChildNode.getPreviousVersion().getId())) {
            throw new NodeRulesException("Incorrect behavior during publish. " +
                    "Tried to increment a node that was not actually a consumer. Increment was attempted on " +
                    this + " and the updated node was " + updatedChildNode);
        }
        copy.getSupportingNodes().add(updatedChildNode);
        return copy;
    }

    @Override
    public AssertionNode createNewDraft(Build build, boolean createBodyDraft) throws NodeRulesException {

        AssertionNode copy;

        if (isDraft()) {
            throw new NodeRulesException("Node is already a draft!");
        }

        if (createBodyDraft) {
            AssertionBody freshBody = new AssertionBody(getBody().getTitle(), getBody().getBody(), build.author, getBody().getMajorVersion());
            VersionHelper.decorateDraftBody(freshBody);
            copy = new AssertionNode(freshBody, build);
        } else {
            copy = new AssertionNode(getBody(), build);
        }

        copy.setSupportingNodes(new HashSet<>(getSupportingNodes()));
        copy.setPreviousVersion(this);

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
