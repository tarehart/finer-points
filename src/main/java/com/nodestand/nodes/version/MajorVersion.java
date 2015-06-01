package com.nodestand.nodes.version;

import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class MajorVersion {

    @GraphId
    protected Long id;

    private int versionNumber;

    @RelatedTo(type="VERSION_OF", direction = Direction.OUTGOING)
    protected VersionAggregator versionAggregator;

    public MajorVersion() {}

    public MajorVersion(int versionNumber, VersionAggregator versionAggregator) {
        this.versionNumber = versionNumber;
        this.versionAggregator = versionAggregator;
    }

    public int getVersionNumber() {
        return versionNumber;
    }
}
