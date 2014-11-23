package com.nodestand.nodes;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

@NodeEntity
public abstract class ArgumentNode {

    @GraphId
    protected Long id;
    public String title;

    public ArgumentNode() {}

    public ArgumentNode(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public abstract String getType();

}
