package com.nodestand.nodes.comment;

import com.nodestand.nodes.User;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

import java.util.HashSet;

@NodeEntity
public class Comment implements Commentable {

    @GraphId
    protected Long id;

    @Relationship(type="AUTHORED_BY", direction = Relationship.OUTGOING)
    public User author;

    @Relationship(type="RESPONDS_TO", direction = Relationship.OUTGOING)
    public Commentable parent;

    @Relationship(type="UPVOTED_BY", direction = Relationship.OUTGOING)
    public HashSet<User> upVoters;

    public String body;

    public Comment() {}

    public long getId() {
        return id;
    }

    public void registerUpVote(User fan) {

        if (upVoters == null) {
            upVoters = new HashSet<>();
        }
        upVoters.add(fan);
    }


    public Comment(Commentable parent, User author, String body) {
        this.parent = parent;
        this.author = author;
        this.body = body;
    }

}
