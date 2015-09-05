package com.nodestand.controllers.serial;

import com.nodestand.nodes.ArgumentNode;

import java.util.*;

public class QuickGraphResponse {

    private Set<Map<String, Object>> nodes;
    private Set<QuickEdge> edges;

    public QuickGraphResponse(Set<Map<String, Object>> nodes, Set<QuickEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Set<Map<String, Object>> getNodes() {
        return nodes;
    }

    public Set<QuickEdge> getEdges() {
        return edges;
    }
}
