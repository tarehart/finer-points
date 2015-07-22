package com.nodestand.nodes.source;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.version.Build;

public class SourceNode extends ArgumentNode {
    private static final String TYPE = "source";

    public SourceNode() {};

    public SourceNode(SourceBody body, Build build) {
        super(body, build);
    }

    @Override
    public String getType() {
        return TYPE;
    }

    public SourceBody getBody() {
        return (SourceBody) body;
    }
}
