package com.nodestand.controllers.serial;

import com.nodestand.nodes.comment.Commentable;

import java.util.Set;

public class QuickCommentResponse {

    private Set<Commentable> nodes;
    private Set<QuickEdge> edges;
    private Long bodyId;

    public QuickCommentResponse(Set<Commentable> nodes, Set<QuickEdge> edges, Long bodyId) {
        this.nodes = nodes;
        this.edges = edges;
        this.bodyId = bodyId;
    }

    public Set<Commentable> getNodes() {
        return nodes;
    }

    public Set<QuickEdge> getEdges() {
        return edges;
    }

    public Long getBodyId() {
        return bodyId;
    }
}
