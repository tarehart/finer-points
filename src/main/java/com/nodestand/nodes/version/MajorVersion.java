package com.nodestand.nodes.version;

import com.nodestand.util.IdGenerator;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.Index;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Relationship;

@NodeEntity
public class MajorVersion {

    @GraphId
    protected Long id;

    @Index
    private String stableId;

    private int versionNumber;

    @Relationship(type="VERSION_OF", direction = Relationship.OUTGOING)
    protected VersionAggregator versionAggregator;

    public MajorVersion() {}

    public MajorVersion(int versionNumber, VersionAggregator versionAggregator) {
        this.versionNumber = versionNumber;
        this.versionAggregator = versionAggregator;
        this.stableId = IdGenerator.newId();
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public Long getId() {
        return id;
    }

    public String getStableId() {
        return stableId;
    }
}
