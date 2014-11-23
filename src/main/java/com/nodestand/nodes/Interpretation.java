package com.nodestand.nodes;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class Interpretation extends ArgumentNode {
    private static final String TYPE = "interpretation";

    @RelatedTo(type="INTERPRETS", direction = Direction.OUTGOING)
    Source source;

    public Interpretation() {}

    public Interpretation(String title) {
        super(title);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public void setSource(Source source) {
        this.source = source;
    }
}
