package com.nodestand.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.vote.ArgumentVote;
import com.nodestand.nodes.vote.VoteType;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@NodeEntity
public class User {
    @GraphId Long nodeId;

    // TODO: this damned thing won't load from the database. Tried it with and without the @Relationship.
    @Relationship(type="ARGUMENT_VOTE", direction = Relationship.OUTGOING)
    private Set<ArgumentVote> argumentVotes;

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
        }

        argumentVotes.removeIf(v -> v.argumentBody.getId().equals(body.getId()));
    }

    @Override
    public int hashCode() {

        return nodeId != null ? nodeId.hashCode() : super.hashCode();
    }
}
