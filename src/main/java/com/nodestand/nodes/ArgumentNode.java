package com.nodestand.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.service.VersionHelper;
import com.nodestand.util.IdGenerator;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Objects;
import java.util.Set;

@NodeEntity
public abstract class ArgumentNode {

    @GraphId
    protected Long id;

    private String stableId;

    protected int buildVersion = -1;

    @Relationship(type="DEFINED_BY", direction = Relationship.OUTGOING)
    protected ArgumentBody body;

    @Relationship(type="PRECEDED_BY", direction = Relationship.OUTGOING)
    protected ArgumentNode previousVersion;

    @Relationship(type="PRECEDED_BY", direction = Relationship.INCOMING)
    protected Set<ArgumentNode> subsequentVersions;

    public ArgumentNode() {}

    public ArgumentNode(ArgumentBody body) {
        this.body = body;
        this.stableId = IdGenerator.newId();
    }

    public abstract ArgumentBody getBody();

    public void setBody(ArgumentBody body) {
        this.body = body;
    }

    public void setVersion(int buildVersion) {
        this.buildVersion = buildVersion;
    }

    public int getBuildVersion() {
        return buildVersion;
    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    @Relationship(type="PRECEDED_BY", direction = Relationship.INCOMING)
    public Set<ArgumentNode> getSubsequentVersions() {
        return subsequentVersions;
    }

    /**
     * Omissions are OK, false positives are not. It's mostly here to be used by the object graph mapper and to mitigate this issue:
     * https://github.com/neo4j/neo4j-ogm/issues/38
     */
    @Relationship(type="PRECEDED_BY", direction = Relationship.INCOMING)
    public void setSubsequentVersions(Set<ArgumentNode> subsequentVersions) {
        this.subsequentVersions = subsequentVersions;
    }

    public abstract String getType();

    public abstract void alterToPointToChild(ArgumentNode replacementChild, ArgumentNode existingChildNode) throws NodeRulesException;

    /**
     * This should be usable in a scenario where this node is a temporary repository of user edits destined for
     * the target node. It should not muck around with any metadata, just user-editable stuff.
     */
    public abstract void copyContentTo(ArgumentNode target) throws NodeRulesException;

    protected void setupDraftBody(ArgumentBody freshBody) {
        freshBody.originalAuthor = body.originalAuthor;
        freshBody.setPreviousVersion(body);
        VersionHelper.decorateDraftBody(freshBody);
    }

    public abstract ArgumentNode createNewDraft(User author) throws NodeRulesException;

    public ArgumentNode getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(ArgumentNode previousVersion) {
        this.previousVersion = previousVersion;
    }

    protected boolean shouldEditInPlace() {
        return !body.isPublic();
    }

    public boolean isFinalized() {
        return buildVersion >= 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        return Objects.equals(this.getId(), ((ArgumentNode) other).getId());
    }

    @JsonIgnore
    public abstract Set<ArgumentNode> getGraphChildren();

    @JsonIgnore
    public abstract Set<? extends ArgumentNode> getDependentNodes();

    public String getStableId() {
        return stableId;
    }

    @Override
    public String toString() {
        return "[" + getType() + " " + id + " " + stableId + " " + body.getTitle() + "]";
    }
}
