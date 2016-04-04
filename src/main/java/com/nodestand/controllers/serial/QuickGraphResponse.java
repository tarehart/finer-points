package com.nodestand.controllers.serial;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nodestand.nodes.ArgumentNode;

import java.util.Set;

public class QuickGraphResponse {

    private Set<ArgumentNode> nodes;
    private Set<QuickEdge> edges;
    private Long rootId;
    private String rootStableId;

    public QuickGraphResponse(Set<ArgumentNode> nodes, Set<QuickEdge> edges, Long rootId, String rootStableId) {
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

    public Long getRootId() {
        return rootId;
    }

    @JsonIgnore
    public ArgumentNode getRootNode() {
        return nodes.stream().filter(n -> n.getId().equals(rootId)).findFirst().get();
    }
}
