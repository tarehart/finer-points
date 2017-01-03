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
import org.springframework.security.core.GrantedAuthority;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@NodeEntity
public class Author {
    @GraphId Long nodeId;

    String displayName;

    private String stableId;

    private long nodePoints;

    @Relationship(type = "CONTROLLED_BY", direction = Relationship.OUTGOING)
    private User user;

    public Author() {
    }

    public Author(User user, String displayName) {
        this.user = user;
        this.displayName = displayName;
        this.stableId = IdGenerator.newId();
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getDisplayName() {
        return displayName;
    }


    public String getStableId() {
        return stableId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author user = (Author) o;
        if (nodeId == null) return super.equals(o);
        return nodeId.equals(user.nodeId);

    }

    public Long getNodeId() {
        return nodeId;
    }

    @Override
    public int hashCode() {

        return nodeId != null ? nodeId.hashCode() : super.hashCode();
    }

    @JsonIgnore
    public User getUser() {
        return user;
    }

    public void awardNodePoints(int points) {
        this.nodePoints += points;
    }

    public long getNodePoints() {
        return nodePoints;
    }
}
