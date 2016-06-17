package com.nodestand.nodes;

import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.vote.ArgumentVote;
import com.nodestand.nodes.vote.VoteType;
import com.nodestand.service.VersionHelper;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

import java.util.Date;
import java.util.Set;

@NodeEntity
public abstract class ArgumentBody implements Commentable {

    @GraphId
    protected Long id;

    private String title;

    // Justification for the existence of this body if it is in competition with another body with
    // similar title. Examples: "Original version", "Formal logic style", "Curated by Hershey's"
    private String qualifier;

    @Relationship(type="AUTHORED_BY", direction = Relationship.OUTGOING)
    public User author;

    @Relationship(type="ORIGINAL_AUTHOR", direction = Relationship.OUTGOING)
    public User originalAuthor;

    @Relationship(type="VERSION_OF", direction = Relationship.OUTGOING)
    private MajorVersion majorVersion;

    @Relationship(type="PRECEDED_BY", direction = Relationship.OUTGOING)
    protected ArgumentBody previousVersion;

    @Relationship(type="ARGUMENT_VOTE", direction = Relationship.INCOMING)
    private Set<ArgumentVote> argumentVotes;

    // I'm not using a map for this because you're not allowed to save maps on nodes.
    public int greatVotes;
    public int weakVotes;
    public int toucheVotes;
    public int trashVotes;

    private int minorVersion;

    @DateLong
    private Date dateCreated;

    @DateLong
    private Date dateEdited;

    private boolean isEditable = true;

    private boolean isPublic = false;

    public ArgumentBody() {}

    public ArgumentBody(String title, String qualifier, User author, MajorVersion majorVersion) {
        this.title = title;
        this.qualifier = qualifier;
        this.author = author;
        this.originalAuthor = author;
        this.majorVersion = majorVersion;
        this.dateCreated = new Date();

        VersionHelper.decorateDraftBody(this);
    }

    @Override
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

    public void decrementVote(VoteType voteType) throws NodeRulesException {
        switch (voteType) {
            case GREAT:
                greatVotes--;
                break;
            case WEAK:
                weakVotes--;
                break;
            case TOUCHE:
                toucheVotes--;
                break;
            case TRASH:
                trashVotes--;
                break;
            default:
                throw new NodeRulesException("Unexpected vote type: " + voteType.name());
        }
    }

    public void incrementVote(VoteType voteType) throws NodeRulesException {
        switch (voteType) {
            case GREAT:
                greatVotes++;
                break;
            case WEAK:
                weakVotes++;
                break;
            case TOUCHE:
                toucheVotes++;
                break;
            case TRASH:
                trashVotes++;
                break;
            default:
                throw new NodeRulesException("Unexpected vote type: " + voteType.name());
        }
    }
}
