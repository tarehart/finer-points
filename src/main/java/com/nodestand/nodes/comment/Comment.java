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

    public int score;

    public String body;

    public Comment() {}

    @Override
    public Long getId() {
        return id;
    }

    public void modifyScore(int delta) {
        score += delta;
    }

    public Comment(Commentable parent, User author, String body) {
        this.parent = parent;
        this.author = author;
        this.body = body;
    }

}
