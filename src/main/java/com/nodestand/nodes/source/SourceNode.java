package com.nodestand.nodes.source;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.service.VersionHelper;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

public class SourceNode extends ArgumentNode {
    public final String type = "source";

    @Relationship(type="INTERPRETS", direction = Relationship.INCOMING)
    private Set<InterpretationNode> dependentNodes;

    public SourceNode() {};

    public SourceNode(SourceBody body) {
        super(body);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public void alterToPointToChild(ArgumentNode replacementChild, ArgumentNode existingChildNode) throws NodeRulesException {
        // Do nothing.
    }

    @Override
    public void copyContentTo(ArgumentNode target) throws NodeRulesException {
        // Do nothing
    }

    @Override
    public SourceBody createDraftBody(User author, boolean install) throws NodeRulesException {
        SourceBody freshBody = new SourceBody(getBody().getTitle(), author, getBody().getUrl(), getBody().getMajorVersion());
        VersionHelper.decorateDraftBody(freshBody);

        if (install) {
            installBody(freshBody);
        }

        return freshBody;
    }

    @Override
    public SourceNode createNewDraft(User author, boolean createBodyDraft) throws NodeRulesException {
        SourceNode copy;

        if (!body.isPublic()) {
            throw new NodeRulesException("Node is already a draft!");
        }

        if (createBodyDraft) {
            SourceBody freshBody = createDraftBody(author, false);
            copy = new SourceNode(freshBody);
        } else {
            copy = new SourceNode(getBody());
        }

        copy.setPreviousVersion(this);

        return copy;
    }

    @Override
    public Set<ArgumentNode> getGraphChildren() {
        return new HashSet<>(0);
    }

    public SourceBody getBody() {
        return (SourceBody) body;
    }

    @Override
    @JsonIgnore
    @Relationship(type="INTERPRETS", direction = Relationship.INCOMING)
    public Set<InterpretationNode> getDependentNodes() {
        return dependentNodes;
    }

    /**
     * Omissions are OK, false positives are not. It's mostly here to be used by the object graph mapper and to mitigate this issue:
     * https://github.com/neo4j/neo4j-ogm/issues/38
     */
    @Relationship(type="INTERPRETS", direction = Relationship.INCOMING)
    public void setDependentNodes(Set<InterpretationNode> dependentNodes) {
        this.dependentNodes = dependentNodes;
    }
}
