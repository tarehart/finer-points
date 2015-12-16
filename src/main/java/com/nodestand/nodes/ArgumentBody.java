package com.nodestand.nodes;

import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.version.VersionHelper;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

import java.util.Date;

@NodeEntity
public abstract class ArgumentBody implements Commentable {

    @GraphId
    protected Long id;

    private String title;

    @Relationship(type="AUTHORED_BY", direction = Relationship.OUTGOING)
    public User author;

    @Relationship(type="EDITED_BY", direction = Relationship.OUTGOING)
    public User editor;

    @Relationship(type="VERSION_OF", direction = Relationship.OUTGOING)
    private MajorVersion majorVersion;

//    @Relationship(type="DEFINED_BY", direction = Relationship.INCOMING)
//    protected Set<ArgumentNode> dependentNodes;

    private int minorVersion;

    @DateLong
    private Date dateCreated;

    @DateLong
    private Date dateEdited;

    private boolean isEditable = true;

    private boolean isPublic = false;

    public ArgumentBody() {}

    public ArgumentBody(String title, User author) {
        this(title, author, null);
    }

    public ArgumentBody(String title, User author, MajorVersion majorVersion) {
        this.title = title;
        this.author = author;
        this.majorVersion = majorVersion;
        this.dateCreated = new Date();

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

    public void applyEditTo(ArgumentBody targetBody) {
        targetBody.title = title;
        targetBody.editor = author;
        targetBody.dateEdited = new Date();
    };

    public void setMinorVersion(int minorVersion) {
        this.minorVersion = minorVersion;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public boolean isEditable() {
        return isEditable;
    }

    public void setIsEditable(boolean isEditable) {
        this.isEditable = isEditable;
    }

    public void setTitle(String title) throws ImmutableNodeException {
        this.title = title;
    }

    public void setMajorVersion(MajorVersion majorVersion) {
        this.majorVersion = majorVersion;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Date getDateEdited() {
        return dateEdited;
    }

    public void setDateEdited(Date dateEdited) {
        this.dateEdited = dateEdited;
    }
}
