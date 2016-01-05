package com.nodestand.nodes;

import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.version.VersionHelper;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

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

    /*
     In CypherContext.java, I've found that the registeredRelationships field contains relationships that are quite
     wrong, e.g. startNodeId does not match startNodeType. Probably a bug in the OGM.

     GraphEntityMapper.java:335 is calling in with mismatched id and type. That might be the source of the problem.

     You can kick off this behavior by voting for everything (nothing gets cleared) and then voting great.
     */
    @Relationship(type="VOTE_GREAT", direction = Relationship.INCOMING)
    private Set<User> greatVoters;

    public Integer greatVotes;

    @Relationship(type="VOTE_WEAK", direction = Relationship.INCOMING)
    private Set<User> weakVoters;

    public Integer weakVotes;

    @Relationship(type="VOTE_TOUCHE", direction = Relationship.INCOMING)
    private Set<User> toucheVoters;

    public Integer toucheVotes;

    @Relationship(type="VOTE_TRASH", direction = Relationship.INCOMING)
    private Set<User> trashVoters;

    public Integer trashVotes;

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

    /**
     * Votes MUST be loaded first, or we won't unregister properly.
     */
    public void registerGreatVote(User voter) throws NodeRulesException {
        clearExistingVote(voter);
        greatVoters.add(voter);
        greatVotes++;
    }

    /**
     * Votes MUST be loaded first, or we won't unregister properly.
     */
    public void registerWeakVote(User voter) throws NodeRulesException {
        clearExistingVote(voter);
        weakVoters.add(voter);
        weakVotes++;
    }

    /**
     * Votes MUST be loaded first, or we won't unregister properly.
     */
    public void registerToucheVote(User voter) throws NodeRulesException {
        clearExistingVote(voter);
        toucheVoters.add(voter);
        toucheVotes++;
    }

    /**
     * Votes MUST be loaded first, or we won't unregister properly.
     */
    public void registerTrashVote(User voter) {
        clearExistingVote(voter);
        trashVoters.add(voter);
        trashVotes++;
    }

    private void clearExistingVote(User voter) {
        if (greatVoters == null) {
            greatVoters = new HashSet<>();
            greatVotes = 0;
        }
        if (weakVoters == null) {
            weakVoters = new HashSet<>();
            weakVotes = 0;
        }
        if (toucheVoters == null) {
            toucheVoters = new HashSet<>();
            toucheVotes = 0;
        }
        if (trashVoters == null) {
            trashVoters = new HashSet<>();
            trashVotes = 0;
        }

        if (greatVoters.remove(voter)) {
            greatVotes--;
        }
        if (weakVoters.remove(voter)) {
            weakVotes--;
        }
        if (toucheVoters.remove(voter)) {
            toucheVotes--;
        }
        if (trashVoters.remove(voter)) {
            trashVotes--;
        }
    }

    /**
     * Votes MUST be loaded first, or we won't unregister properly.
     */
    public void revokeVote(User user) {
        clearExistingVote(user);
    }
}
