package com.nodestand.nodes;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

import java.util.HashSet;
import java.util.Set;


@NodeEntity
public class Assertion {

    @GraphId private Long id;

    public String title;

    @RelatedTo(type="SUPPORTED_BY", direction = Direction.OUTGOING)
    Set<Assertion> supportingAssertions;

    public Assertion() {}

    public Assertion(String title) {
        this.title = title;
    }

    public Long getId() {
        return id;
    }

    public void supportedBy(Assertion a) {
        if (supportingAssertions == null) {
            supportingAssertions = new HashSet<>();
        }
        supportingAssertions.add(a);
    }

}
