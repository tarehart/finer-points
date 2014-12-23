package com.nodestand.nodes;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;
import org.springframework.web.bind.annotation.ModelAttribute;

@NodeEntity
public abstract class ArgumentNode implements Commentable {

    @GraphId
    protected Long id;
    public String title;

    @RelatedTo(type="AUTHORED_BY", direction = Direction.OUTGOING)
    public User author;

    public ArgumentNode() {}

    public ArgumentNode(String title, User author) {
        this.title = title;
        this.author = author;
    }

    public Long getId() {
        return id;
    }

    public abstract String getType();

}
