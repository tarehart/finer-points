package com.nodestand.nodes.version;

import com.nodestand.nodes.User;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class Build {

    @GraphId
    protected Long id;

    @RelatedTo(type = "AUTHORED_BY", direction = Direction.OUTGOING)
    public User author;

    public Build() {
    }

    public Long getId() {
        return id;
    }

}