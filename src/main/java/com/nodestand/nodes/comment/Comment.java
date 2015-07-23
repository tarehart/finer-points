package com.nodestand.nodes.comment;

import com.nodestand.nodes.User;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import java.util.HashSet;

@NodeEntity
public class Comment implements Commentable {

    @GraphId
    protected Long id;

    @RelatedTo(type="AUTHORED_BY", direction = Direction.OUTGOING)
    public User author;

    @RelatedTo(type="RESPONDS_TO", direction = Direction.OUTGOING)
    public Commentable parent;

    @RelatedTo(type="UPVOTED_BY", direction = Direction.OUTGOING)
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
