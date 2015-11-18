package com.nodestand.controllers.serial;

import com.nodestand.nodes.ArgumentNode;

import java.util.Set;

public class QuickGraphResponse {

    private Set<ArgumentNode> nodes;
    private Set<QuickEdge> edges;
    private long rootId;

    public QuickGraphResponse(Set<ArgumentNode> nodes, Set<QuickEdge> edges, long rootId) {
        this.nodes = nodes;
        this.edges = edges;
        this.rootId = rootId;
    }

    public Set<ArgumentNode> getNodes() {
        return nodes;
    }

    public Set<QuickEdge> getEdges() {
        return edges;
    }

    public long getRootId() {
        return rootId;
    }
}
