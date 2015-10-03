package com.nodestand.nodes;

import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.version.VersionHelper;
import org.joda.time.DateTime;
import org.neo4j.index.impl.lucene.IndexType;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public abstract class ArgumentBody implements Commentable {

    @GraphId
    protected Long id;

    //@Indexed(indexName = "title-search", indexType=IndexType.FULLTEXT)
    private String title;

    @Relationship(type="AUTHORED_BY", direction = Relationship.OUTGOING)
    public User author;

    @Relationship(type="VERSION_OF", direction = Relationship.OUTGOING)
    private MajorVersion majorVersion;

    private int minorVersion;

    private DateTime dateCreated;

    private boolean isDraft = true;

    public ArgumentBody() {}

    public ArgumentBody(String title, User author) {
        this(title, author, null);
    }

    public ArgumentBody(String title, User author, MajorVersion majorVersion) {
        this.title = title;
        this.author = author;
        this.majorVersion = majorVersion;
        this.dateCreated = DateTime.now();

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

    public DateTime getDateCreated() {
        return dateCreated;
    }

    public boolean isDraft() {
        return isDraft;
    }

    public void setIsDraft(boolean isDraft) {
        this.isDraft = isDraft;
    }

    public void setTitle(String title) throws ImmutableNodeException {
        if (!isDraft()) {
            throw new ImmutableNodeException("Cannot edit a title unless the argument is in draft mode. Must create a new version.");
        }
        this.title = title;
    }

    public void setMajorVersion(MajorVersion majorVersion) {
        this.majorVersion = majorVersion;
    }
}
