package com.nodestand.nodes;

import com.nodestand.nodes.version.Build;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.Fetch;
import org.springframework.data.neo4j.annotation.GraphId;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public abstract class ArgumentNode {

    @GraphId
    protected Long id;

    protected int buildVersion;

    @Fetch
    @RelatedTo(type="BUILT_BY", direction = Direction.OUTGOING)
    protected Build build;

    @Fetch
    @RelatedTo(type="DEFINED_BY", direction = Direction.OUTGOING)
    protected ArgumentBody body;

    public ArgumentNode() {}

    public ArgumentNode(ArgumentBody body, Build build) {
        this.body = body;
        this.build = build;
    }

    public ArgumentBody getBody() {
        return body;
    }

    public Build getBuild() {
        return build;
    }

    public void setVersion(int buildVersion) {
        this.buildVersion = buildVersion;
    }

    public Long getId() {
        return id;
    }

    public abstract String getType();

}
