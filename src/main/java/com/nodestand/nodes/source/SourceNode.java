package com.nodestand.nodes.source;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.version.Build;
import org.springframework.beans.MethodInvocationException;

import java.lang.reflect.InvocationTargetException;

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

    @Override
    public ArgumentNode cloneForMinorVersionUpdate(ArgumentNode updatedNode) {
        // You should never use this because there's no need to do
        // build version updates on source nodes; they have no consumers.
        return null;
    }

    public SourceBody getBody() {
        return (SourceBody) body;
    }
}
