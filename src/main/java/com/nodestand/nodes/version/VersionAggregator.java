package com.nodestand.nodes.version;

import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;

@NodeEntity
public class VersionAggregator {

    @GraphId
    protected Long id;

}
