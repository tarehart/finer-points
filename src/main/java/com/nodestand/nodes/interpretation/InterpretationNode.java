package com.nodestand.nodes.interpretation;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.Build;
import org.neo4j.graphdb.Direction;
import org.springframework.data.neo4j.annotation.NodeEntity;
import org.springframework.data.neo4j.annotation.RelatedTo;

@NodeEntity
public class InterpretationNode extends ArgumentNode {
    private static final String TYPE = "interpretation";

    @RelatedTo(type="INTERPRETS", direction = Direction.OUTGOING)
    SourceNode source;

    public InterpretationNode() {}

    public InterpretationNode(InterpretationBody body, Build build) {
        super(body, build);

    }

    @Override
    public String getType() {
        return TYPE;
    }

    public void setSource(SourceNode source) {
        this.source = source;
    }

    public InterpretationBody getBody() {
        return (InterpretationBody) body;
    }
}
