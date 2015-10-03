package com.nodestand.nodes.version;

import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class MajorVersion {

    @GraphId
    protected Long id;

    private int versionNumber;

    @Relationship(type="VERSION_OF", direction = Relationship.OUTGOING)
    protected VersionAggregator versionAggregator;

    public MajorVersion() {}

    public MajorVersion(int versionNumber, VersionAggregator versionAggregator) {
        this.versionNumber = versionNumber;
        this.versionAggregator = versionAggregator;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public Long getId() {
        return id;
    }
}
