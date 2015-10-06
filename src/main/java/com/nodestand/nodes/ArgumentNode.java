package com.nodestand.nodes;

import com.nodestand.nodes.version.Build;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public abstract class ArgumentNode {

    @GraphId
    protected Long id;

    protected int buildVersion = -1;

    @Fetch
    @RelatedTo(type="BUILT_BY", direction = Direction.OUTGOING)
    protected Build build;

    @Fetch
    @RelatedTo(type="DEFINED_BY", direction = Direction.OUTGOING)
    protected ArgumentBody body;

    @RelatedTo(type="PRECEDED_BY", direction = Direction.OUTGOING)
    protected ArgumentNode previousVersion;

    public ArgumentNode() {}

    public ArgumentNode(ArgumentBody body, Build build) {
        this.body = body;
        this.build = build;
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
}
