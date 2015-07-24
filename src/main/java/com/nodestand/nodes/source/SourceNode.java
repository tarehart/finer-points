package com.nodestand.nodes.source;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.version.Build;

public class SourceNode extends ArgumentNode {
    public final String type = "source";

    public SourceNode() {};

    public SourceNode(SourceBody body, Build build) {
        super(body, build);
    }

    @Override
    public String getType() {
        return type;
    }

    public SourceBody getBody() {
        return (SourceBody) body;
    }
}
