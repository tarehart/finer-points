package com.nodestand.controllers.serial;

import com.nodestand.nodes.ArgumentNode;

import java.util.Set;

public class QuickGraphResponse {

    private Set<ArgumentNode> nodes;
    private Set<QuickEdge> edges;
    private long rootId;
    private String rootStableId;

    public QuickGraphResponse(Set<ArgumentNode> nodes, Set<QuickEdge> edges, long rootId, String rootStableId) {
        this.nodes = nodes;
        this.edges = edges;
        this.rootId = rootId;
        this.rootStableId = rootStableId;
    }

    public Set<ArgumentNode> getNodes() {
        return nodes;
    }

    public Set<QuickEdge> getEdges() {
        return edges;
    }

    public String getRootStableId() {
        return rootStableId;
    }

    public long getRootId() {
        return rootId;
    }
}
