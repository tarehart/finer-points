package com.nodestand.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.vote.ArgumentVote;
import com.nodestand.nodes.vote.CommentVote;
import com.nodestand.nodes.vote.VoteType;
import com.nodestand.util.IdGenerator;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.annotation.typeconversion.DateLong;
import org.springframework.security.core.GrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;

@NodeEntity
public class User {
    @GraphId Long nodeId;

    @Relationship(type = "CONTROLLED_BY", direction = Relationship.INCOMING)
    private Set<Author> aliases;

    @Relationship(type="ARGUMENT_VOTE", direction = Relationship.OUTGOING)
    private Set<ArgumentVote> argumentVotes;

    @Transient
    private Map<Long, VoteType> bodyVotes;

    @Relationship(type="COMMENT_VOTE", direction = Relationship.OUTGOING)
    private Set<CommentVote> commentVotes;

    /**
     * Mapping of comment node id to either 1 or -1, representing this user's up-vote or down-vote on that comment.
     *
     * Transient because it does not get persisted in the database.
     */
    @Transient
    private Map<Long, Integer> commentVoteMap;

    private String stableId;

    private Roles[] roles;
    private String providerId;
    private String providerUserId;

    @DateLong
    private Date dateCreated;


    public User() {
    }

    public User(String providerId, String providerUserId, Roles... roles) {
        this.roles = roles;
        this.providerId = providerId;
        this.providerUserId = providerUserId;
        this.stableId = IdGenerator.newId();
        this.aliases = new HashSet<>();
        this.dateCreated = new Date();
    }

    @Override
    public String toString() {
        return providerUserId;
    }

    @JsonIgnore
    public Roles[] getRole() {
        return roles;
    }

    @JsonIgnore
    public String getProviderId() {
        return providerId;
    }

    @JsonIgnore
    public String getProviderUserId() {
        return providerUserId;
    }

    @Relationship(type="ARGUMENT_VOTE", direction = Relationship.OUTGOING)
    public void setArgumentVotes(Set<ArgumentVote> argumentVotes) {
        this.argumentVotes = argumentVotes;
        bodyVotes = argumentVotes.stream().collect(Collectors.toMap(v -> v.majorVersion.getId(), v -> v.voteType));
    }

    @JsonIgnore
    public Map<Long, VoteType> getBodyVotes() {
        return bodyVotes;
    }

    @Relationship(type="COMMENT_VOTE", direction = Relationship.OUTGOING)
    public void setCommentVotes(Set<CommentVote> commentVotes) {
        this.commentVotes = commentVotes;
        commentVoteMap = commentVotes.stream().collect(Collectors.toMap(c -> c.comment.getId(), c -> c.isUpvote ? 1 : -1));
    }

    @JsonIgnore
    public Map<Long, Integer> getCommentVoteMap() {
        return commentVoteMap;
    }

    public String getStableId() {
        return stableId;
    }

    public Set<Author> getAliases() {
        return aliases;
    }

    public void setAliases(Set<Author> aliases) {
        this.aliases = aliases;
    }

    public Author addNewAlias(String displayName) {
        Author author = new Author(this, displayName);
        aliases.add(author);
        return author;
    }

    public enum Roles implements GrantedAuthority {
        ROLE_USER, ROLE_ADMIN;

        @Override
        public String getAuthority() {
            return name();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;
        if (nodeId == null) return super.equals(o);
        return nodeId.equals(user.nodeId);

    }

    public Long getNodeId() {
        return nodeId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Optional<ArgumentVote> getExistingVote(MajorVersion mv) {
        if (argumentVotes == null) {
            argumentVotes = new HashSet<>();
        }

        return argumentVotes.stream().filter(v -> v.majorVersion.getId().equals(mv.getId())).findFirst();
    }

    public void registerNewVote(ArgumentVote newVote) throws NodeRulesException {

        if (argumentVotes == null) {
            argumentVotes = new HashSet<>();
        }

        argumentVotes.add(newVote);
    }


    public void revokeVote(String majorVersionStableId) throws NodeRulesException {
        argumentVotes.removeIf(v -> v.majorVersion.getStableId().equals(majorVersionStableId));
    }

    public void registerCommentVote(Comment comment, boolean isUpvote) {

        if (commentVotes == null) {
            commentVotes = new HashSet<>();
        }

        if (commentVoteMap == null) {
            commentVoteMap = new HashMap<>();
        }

        Optional<CommentVote> existingVote = commentVotes.stream().filter(v -> v.comment.getId().equals(comment.getId())).findFirst();
        int numericRepresentation = isUpvote ? 1 : -1;

        if (existingVote.isPresent()) {
            CommentVote vote = existingVote.get();
            if (vote.isUpvote != isUpvote) {
                commentVoteMap.put(comment.getId(), numericRepresentation);
                comment.modifyScore(numericRepresentation * 2); // We're reversing the direction of a vote, so it's a two-point swing.
                existingVote.get().isUpvote = isUpvote;
            }
        } else {
            CommentVote newVote = new CommentVote();
            newVote.isUpvote= isUpvote;
            newVote.comment = comment;
            newVote.user = this;
            commentVotes.add(newVote);
            commentVoteMap.put(comment.getId(), numericRepresentation);
            comment.modifyScore(numericRepresentation);
        }
    }

    public void revokeCommentVote(Comment comment) throws NodeRulesException {

        Optional<CommentVote> existingVote = commentVotes.stream().filter(v -> v.comment.getId().equals(comment.getId())).findFirst();
        if (existingVote.isPresent()) {
            comment.modifyScore(existingVote.get().isUpvote ? -1 : 1);
            commentVotes.removeIf(v -> v.comment.getId().equals(comment.getId()));
        }
    }


    @Override
    public int hashCode() {

        return nodeId != null ? nodeId.hashCode() : super.hashCode();
    }
}
