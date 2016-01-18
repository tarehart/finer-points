package com.nodestand.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.vote.ArgumentVote;
import com.nodestand.nodes.vote.CommentVote;
import com.nodestand.nodes.vote.VoteType;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@NodeEntity
public class User {
    @GraphId Long nodeId;

    @Relationship(type="ARGUMENT_VOTE", direction = Relationship.OUTGOING)
    private Set<ArgumentVote> argumentVotes;

    private Map<Long, VoteType> bodyVotes;

    @Relationship(type="COMMENT_VOTE", direction = Relationship.OUTGOING)
    private Set<CommentVote> commentVotes;

    private Map<Long, Boolean> commentVoteMap;


    //@Indexed
    String displayName;

    //@Indexed(unique = true)
    String socialId;

    private Roles[] roles;


    public User() {
    }

    public User(String socialId, String displayName, Roles... roles) {
        this.roles = roles;
        this.displayName = displayName;
        this.socialId = socialId;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", displayName, socialId);
    }

    @JsonIgnore
    public Roles[] getRole() {
        return roles;
    }

    public String getDisplayName() {
        return displayName;
    }

    @JsonIgnore
    public String getSocialId() {
        return socialId;
    }

    @Relationship(type="ARGUMENT_VOTE", direction = Relationship.OUTGOING)
    public void setArgumentVotes(Set<ArgumentVote> argumentVotes) {
        this.argumentVotes = argumentVotes;
        bodyVotes = argumentVotes.stream().collect(Collectors.toMap(v -> v.argumentBody.getId(), v -> v.voteType));
    }

    public Map<Long, VoteType> getBodyVotes() {
        return bodyVotes;
    }

    @Relationship(type="COMMENT_VOTE", direction = Relationship.OUTGOING)
    public void setCommentVotes(Set<CommentVote> commentVotes) {
        this.commentVotes = commentVotes;
        commentVoteMap = commentVotes.stream().collect(Collectors.toMap(c -> c.comment.getId(), c -> c.isUpvote));
    }

    public Map<Long, Boolean> getCommentVoteMap() {
        return commentVoteMap;
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

    public void registerVote(ArgumentBody body, VoteType voteType) throws NodeRulesException {

        if (argumentVotes == null) {
            argumentVotes = new HashSet<>();
        }

        Optional<ArgumentVote> existingVote = argumentVotes.stream().filter(v -> v.argumentBody.getId().equals(body.getId())).findFirst();


        if (existingVote.isPresent()) {
            ArgumentVote vote = existingVote.get();
            if (!vote.voteType.equals(voteType)) {
                body.decrementVote(vote.voteType);
                body.incrementVote(voteType);
                existingVote.get().voteType = voteType;
            }
        } else {
            ArgumentVote newVote = new ArgumentVote();
            newVote.voteType = voteType;
            newVote.argumentBody = body;
            newVote.user = this;
            argumentVotes.add(newVote);
            body.incrementVote(voteType);
        }
    }


    public void revokeVote(ArgumentBody body) throws NodeRulesException {

        Optional<ArgumentVote> existingVote = argumentVotes.stream().filter(v -> v.argumentBody.getId().equals(body.getId())).findFirst();
        if (existingVote.isPresent()) {
            body.decrementVote(existingVote.get().voteType);
            argumentVotes.removeIf(v -> v.argumentBody.getId().equals(body.getId()));
        }
    }

    public void registerCommentVote(Comment comment, boolean isUpvote) {

        if (commentVotes == null) {
            commentVotes = new HashSet<>();
        }

        Optional<CommentVote> existingVote = commentVotes.stream().filter(v -> v.comment.getId().equals(comment.getId())).findFirst();

        if (existingVote.isPresent()) {
            CommentVote vote = existingVote.get();
            if (vote.isUpvote != isUpvote) {
                comment.modifyScore(isUpvote ? 2 : -2);
                existingVote.get().isUpvote = isUpvote;
            }
        } else {
            CommentVote newVote = new CommentVote();
            newVote.isUpvote= isUpvote;
            newVote.comment = comment;
            newVote.user = this;
            commentVotes.add(newVote);
            // TODO: update comment vote map
            comment.modifyScore(isUpvote ? 1 : -1);
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
