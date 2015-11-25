package com.nodestand.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.version.VersionHelper;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.time.Instant;
import java.util.Set;

@NodeEntity
public abstract class ArgumentBody implements Commentable {

    @GraphId
    protected Long id;

    private String title;

    @Relationship(type="AUTHORED_BY", direction = Relationship.OUTGOING)
    public User author;

    @Relationship(type="VERSION_OF", direction = Relationship.OUTGOING)
    private MajorVersion majorVersion;

//    @Relationship(type="DEFINED_BY", direction = Relationship.INCOMING)
//    protected Set<ArgumentNode> dependentNodes;

    private int minorVersion;

    private Instant dateCreated;

    private boolean isDraft = true;

    public ArgumentBody() {}

    public ArgumentBody(String title, User author) {
        this(title, author, null);
    }

    public ArgumentBody(String title, User author, MajorVersion majorVersion) {
        this.title = title;
        this.author = author;
        this.majorVersion = majorVersion;
        this.dateCreated = Instant.now();

        VersionHelper.decorateDraftBody(this);
    }

    public long getId() {
        return id;
    }

    public void setVersion(MajorVersion majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public String getTitle() {
        return title;
    }

    public MajorVersion getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public abstract ArgumentNode constructNode(VersionHelper versionHelper);

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setIsDraft(boolean isDraft) {
        this.isDraft = isDraft;
    }

    public void setTitle(String title) throws ImmutableNodeException {
        this.title = title;
    }

    public void setMajorVersion(MajorVersion majorVersion) {
        this.majorVersion = majorVersion;
    }

//    @JsonIgnore
//    @Relationship(type="DEFINED_BY", direction = Relationship.INCOMING)
//    public Set<ArgumentNode> getDependentNodes() {
//        return dependentNodes;
//    }
//
//    /**
//     * Omissions are OK, false positives are not. It's mostly here to be used by the object graph mapper and to mitigate this issue:
//     * https://github.com/neo4j/neo4j-ogm/issues/38
//     */
//    @Relationship(type="DEFINED_BY", direction = Relationship.INCOMING)
//    public void setDependentNodes(Set<ArgumentNode> dependentNodes) {
//        this.dependentNodes = dependentNodes;
//    }
}
