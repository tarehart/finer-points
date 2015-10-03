package com.nodestand.nodes.version;

import com.nodestand.nodes.User;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;


/**
 * A Build is created when a draft is created, not when it is published. However, it will probably be
 * assigned to many upstream nodes on publish.
 *
 * TODO: Maybe link to the associated argument body insted of the author.
 * We would still get access to the author and we get insight into what triggered the build.
 */
@NodeEntity
public class Build {

    @GraphId
    protected Long id;

    @Relationship(type = "AUTHORED_BY", direction = Relationship.OUTGOING)
    public User author;

    public Build() {
    }

    public Long getId() {
        return id;
    }

}