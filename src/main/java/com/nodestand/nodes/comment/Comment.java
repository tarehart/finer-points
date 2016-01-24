package com.nodestand.nodes.comment;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.User;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.typeconversion.DateLong;

import java.util.Date;
import java.util.HashSet;

@NodeEntity
public class Comment implements Commentable {

    @GraphId
    protected Long id;

    @Relationship(type="AUTHORED_BY", direction = Relationship.OUTGOING)
    public User author;

    @JsonIgnore
    @Relationship(type="RESPONDS_TO", direction = Relationship.OUTGOING)
    public Commentable parent;

    public int score;

    public String body;

    @DateLong
    private Date dateCreated;

    @DateLong
    private Date dateEdited;

    public Comment() {
        // don't set date created here because this constructor is used when mapping from the database.
    }

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

        this.dateCreated = new Date();
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public Date getDateEdited() {
        return dateEdited;
    }

    public void setDateEdited(Date dateEdited) {
        this.dateEdited = dateEdited;
    }
}
