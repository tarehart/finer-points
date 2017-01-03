package com.nodestand.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.service.VersionHelper;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

import java.util.Date;
import java.util.Set;

@NodeEntity
public abstract class ArgumentBody {

    @GraphId
    protected Long id;

    private String title;

    /**
     * Justification for the existence of this body if it is in competition with another body with
     * similar title. Examples: "Original version", "Formal logic style", "Curated by Hershey's"
     *
     * This might appear to belong on a {@link MajorVersion} instead, but I'm keeping it here because I
     * want it to be editable in a predictable way.
     */
    private String qualifier;

    // From the big picture perspective, i.e. when looking at a major version in the UI, this will
    // be more of an "edited by" field.
    @Relationship(type="AUTHORED_BY", direction = Relationship.OUTGOING)
    public Author author;

    @Relationship(type="VERSION_OF", direction = Relationship.OUTGOING)
    private MajorVersion majorVersion;

    @Relationship(type="DEFINED_BY", direction = Relationship.INCOMING)
    private ArgumentNode node;

    /**
     * We use this for tracking edit history.
     */
    @Relationship(type="PRECEDED_BY", direction = Relationship.OUTGOING)
    protected ArgumentBody previousVersion;

    private int minorVersion;

    @DateLong
    private Date dateCreated;

    @DateLong
    private Date dateEdited;

    private boolean isEditable = true;

    private boolean isPublic = false;

    public ArgumentBody() {}

    public ArgumentBody(String title, String qualifier, Author author, MajorVersion majorVersion) {
        this.title = title;
        this.qualifier = qualifier;
        this.author = author;
        this.majorVersion = majorVersion;
        this.dateCreated = new Date();

        VersionHelper.decorateDraftBody(this);
    }

    public Long getId() {
        return id;
    }

    public void setVersion(MajorVersion majorVersion, int minorVersion) {
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }

    public String getTitle() {
        return title;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public MajorVersion getMajorVersion() {
        return majorVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    public abstract ArgumentNode constructNode();

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

    public ArgumentBody getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(ArgumentBody previousVersion) {
        this.previousVersion = previousVersion;
    }

    @JsonIgnore
    public ArgumentNode getNode() {
        return node;
    }

    public void setNode(ArgumentNode node) {
        this.node = node;
    }

    @JsonIgnore
    public abstract Set<String> getMajorVersionsFromBodyText();
}
