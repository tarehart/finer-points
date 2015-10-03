package com.nodestand.nodes.version;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;

@NodeEntity
public class VersionAggregator {

    @GraphId
    protected Long id;

}
