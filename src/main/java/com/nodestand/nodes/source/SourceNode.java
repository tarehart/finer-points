package com.nodestand.nodes.source;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.version.Build;
import com.nodestand.nodes.version.VersionHelper;
import org.springframework.beans.MethodInvocationException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

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
    public ArgumentNode alterOrCloneToPointToChild(ArgumentNode updatedChildNode) {
        // You should never use this because there's no need to do
        // build version updates on source nodes; they have no consumers.
        return null;
    }

    @Override
    public SourceNode createNewDraft(Build build, boolean createBodyDraft) throws NodeRulesException {
        SourceNode copy;

        if (isDraft()) {
            throw new NodeRulesException("Node is already a draft!");
        }

        if (createBodyDraft) {
            SourceBody freshBody = new SourceBody(getBody().getTitle(), build.author, getBody().getUrl(), getBody().getMajorVersion());
            VersionHelper.decorateDraftBody(freshBody);
            copy = new SourceNode(freshBody, build);
        } else {
            copy = new SourceNode(getBody(), build);
        }

        copy.setPreviousVersion(this);

        return copy;
    }

    @Override
    public Set<ArgumentNode> getGraphChildren() {
        return new HashSet<>(0);
    }

    public SourceBody getBody() {
        return (SourceBody) body;
    }
}
