package com.nodestand.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.version.Build;
import com.nodestand.util.IdGenerator;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.Set;

@NodeEntity
public abstract class ArgumentNode {

    @GraphId
    protected Long id;

    @Index
    private String stableId;

    protected int buildVersion = -1;

    @Relationship(type="BUILT_BY", direction = Relationship.OUTGOING)
    protected Build build;

    @Relationship(type="DEFINED_BY", direction = Relationship.OUTGOING)
    protected ArgumentBody body;

    @Relationship(type="PRECEDED_BY", direction = Relationship.OUTGOING)
    protected ArgumentNode previousVersion;

    public ArgumentNode() {}

    public ArgumentNode(ArgumentBody body, Build build) {
        this.body = body;
        this.build = build;
        this.stableId = IdGenerator.newId();
    }

    public abstract ArgumentBody getBody();

    public Build getBuild() {
        return build;
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

    public abstract String getType();

    /**
     * This will set the body and build properly, but it does not address the buildVersion property. That must be
     * taken care of separately.
     *
     * If the node is a draft, this will not actually produce a clone, it will just modify the draft in place and
     * return it.
     */
    public abstract ArgumentNode alterOrCloneToPointToChild(ArgumentNode updatedChildNode) throws NodeRulesException;

    /**
     * This is useful for when the node is already a draft but the body is not. That situation can arise when a
     * child of this node has been edited. If we are in that state and then receive an edit for the title or content,
     * you'll want to create a draft body to hold that edit.
     * @param author the author of this new body
     * @param install true if you want the new draft body to replace this node's existing body.
     * @return
     */
    public abstract ArgumentBody createDraftBody(User author, boolean install) throws NodeRulesException;

    public abstract ArgumentNode createNewDraft(Build build, boolean createBodyDraft) throws NodeRulesException;

    public void setBuild(Build build) {
        this.build = build;
    }

    public ArgumentNode getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(ArgumentNode previousVersion) {
        this.previousVersion = previousVersion;
    }

    /**
     * We edit in place if it's a draft, or if this node has already been touched by this same build.
     * The latter can happen if there's a tree like this:
     * A - B
     *  \   \
     *   C - D
     *
     * @param buildInProgress
     * @return
     */
    protected boolean shouldEditInPlace(Build buildInProgress) {
        return isDraft() || getBuild().equals(buildInProgress);
    }

    protected void installBody(ArgumentBody freshBody) throws NodeRulesException {
        if (!isDraft()) {
            throw new NodeRulesException("Cannot install a draft body on a node that is not itself a draft!");
        }
        body = freshBody;
    }

    public boolean isDraft() {
        return buildVersion < 0;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (!this.getClass().equals(other.getClass())) {
            return false;
        }
        return this.getId() == ((ArgumentNode)other).getId();
    }

    @JsonIgnore
    public abstract Set<ArgumentNode> getGraphChildren();

    public String getStableId() {
        return stableId;
    }
}
