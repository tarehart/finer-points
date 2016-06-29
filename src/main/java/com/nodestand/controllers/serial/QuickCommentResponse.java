package com.nodestand.controllers.serial;

import com.nodestand.nodes.comment.Commentable;

import java.util.Set;

public class QuickCommentResponse {

    private Set<Commentable> nodes;
    private Set<QuickEdge> edges;
    private Long bodyId;

    public QuickCommentResponse(Set<Commentable> nodes, Set<QuickEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Set<Commentable> getNodes() {
        return nodes;
    }

    public Set<QuickEdge> getEdges() {
        return edges;
    }
}
