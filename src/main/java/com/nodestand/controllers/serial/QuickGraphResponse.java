package com.nodestand.controllers.serial;

import java.util.Map;
import java.util.Set;

public class QuickGraphResponse {

    private Set<Map<String, Object>> nodes;
    private Set<QuickEdge> edges;
    private long rootId;

    public QuickGraphResponse(Set<Map<String, Object>> nodes, Set<QuickEdge> edges, long rootId) {
        this.nodes = nodes;
        this.edges = edges;
        this.rootId = rootId;
    }

    public Set<Map<String, Object>> getNodes() {
        return nodes;
    }

    public Set<QuickEdge> getEdges() {
        return edges;
    }

    public long getRootId() {
        return rootId;
    }
}
