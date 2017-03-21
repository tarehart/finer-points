package com.nodestand.nodes.subject;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.*;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.source.SourceBody;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;
import java.util.Set;

public class SubjectNode extends ArgumentNode implements LeafNode {

    @Relationship(type="INTERPRETS", direction = Relationship.INCOMING)
    private Set<InterpretationNode> dependentNodes;

    public SubjectNode() {}

    public SubjectNode(SubjectBody body) {
        super(body);
    }

    @Override
    public String getType() {
        return "subject";
    }

    @Override
    public void alterToPointToChild(Node replacementChild, Node existingChildNode) throws NodeRulesException {
        // Do nothing.
    }

    @Override
    public void copyContentTo(Node target) throws NodeRulesException {
        // Do nothing
    }

    private SubjectBody createDraftBody(Author author) throws NodeRulesException {
        SubjectBody freshBody = new SubjectBody(getBody().getTitle(), getBody().getQualifier(), author, getBody().getUrl(), getBody().getMajorVersion());
        setupDraftBody(freshBody);
        return freshBody;
    }

    @Override
    public SubjectNode createNewDraft(Author author) throws NodeRulesException {

        if (!body.isPublic()) {
            throw new NodeRulesException("Node is already a draft!");
        }

        SubjectBody freshBody = createDraftBody(author);

        SubjectNode copy = new SubjectNode(freshBody);
        copy.setPreviousVersion(this);

        return copy;
    }

    @Override
    public Set<Node> getGraphChildren() {
        return new HashSet<>(0);
    }

    public SubjectBody getBody() {
        return (SubjectBody) body;
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
