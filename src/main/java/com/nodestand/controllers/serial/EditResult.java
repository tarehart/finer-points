package com.nodestand.controllers.serial;

import com.nodestand.nodes.ArgumentNode;

public class EditResult {

    private QuickGraphResponse graph;
    private ArgumentNode editedNode;

    public EditResult(ArgumentNode editedNode) {
        this.editedNode = editedNode;
    }

    public QuickGraphResponse getGraph() {
        return graph;
    }

    public void setGraph(QuickGraphResponse graph) {
        this.graph = graph;
    }

    public ArgumentNode getEditedNode() {
        return editedNode;
    }
}
