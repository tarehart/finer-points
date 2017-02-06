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
public abstract class ArgumentNode implements Node {

    @GraphId
    protected Long id;

    private String stableId;

    protected int buildVersion = -1;

    @Relationship(type="DEFINED_BY", direction = Relationship.OUTGOING)
    protected ArgumentBody body;

    /**
     * This is just around to facilitate some publish logic. At the moment,
     * it gets obliterated after publish.
     */
    @Relationship(type="PRECEDED_BY", direction = Relationship.OUTGOING)
    protected Node previousVersion;

    /**
     * This is really just here to make the relationship two-way, which can avoid certain
     * bugs in Neo4j OGM.
     */
    @Relationship(type="PRECEDED_BY", direction = Relationship.INCOMING)
    protected Set<Node> subsequentVersions;

    public ArgumentNode() {}

    public ArgumentNode(ArgumentBody body) {
        this.body = body;
        this.stableId = IdGenerator.newId();
    }

    public abstract ArgumentBody getBody();

    public void setBody(ArgumentBody body) {
        assert body == null || body.getNode() == null || body.getNode() == this; // Relationship should be reciprocal or absent
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
    public Set<Node> getSubsequentVersions() {
        return subsequentVersions;
    }

    /**
     * Omissions are OK, false positives are not. It's mostly here to be used by the object graph mapper and to mitigate this issue:
     * https://github.com/neo4j/neo4j-ogm/issues/38
     */
    @Relationship(type="PRECEDED_BY", direction = Relationship.INCOMING)
    public void setSubsequentVersions(Set<Node> subsequentVersions) {
        this.subsequentVersions = subsequentVersions;
    }

    public abstract String getType();

    protected void setupDraftBody(ArgumentBody freshBody) {
        freshBody.setPreviousVersion(body);
        VersionHelper.decorateDraftBody(freshBody);
    }

    public abstract ArgumentNode createNewDraft(Author author) throws NodeRulesException;

    public Node getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(Node previousVersion) {
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
    public abstract Set<Node> getGraphChildren();

    @JsonIgnore
    public abstract Set<? extends Node> getDependentNodes();

    public String getStableId() {
        return stableId;
    }

    @Override
    public String toString() {
        return "[" + getType() + " " + id + " " + stableId + " " + (body != null ? body.getTitle() : "(no body)") + "]";
    }
}
