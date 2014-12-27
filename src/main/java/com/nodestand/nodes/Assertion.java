package com.nodestand.nodes;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import java.util.HashSet;
import java.util.Set;


@NodeEntity
public class Assertion extends ArgumentNode {

    private static final String TYPE = "assertion";

    @RelatedTo(type="SUPPORTED_BY", direction = Direction.OUTGOING)
    Set<ArgumentNode> supportingNodes;

    String body;

    public Assertion() {}

    public Assertion(String title, String body, User author) {
        super(title, author);

        this.body = body;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public void supportedBy(ArgumentNode a) {

        assert !(a instanceof Source);

        if (supportingNodes == null) {
            supportingNodes = new HashSet<>();
        }
        supportingNodes.add(a);
    }
}
