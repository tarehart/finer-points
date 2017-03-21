package com.nodestand.nodes;

import com.nodestand.nodes.interpretation.InterpretationNode;

import java.util.HashSet;
import java.util.Set;

public interface LeafNode extends Node {
    Set<InterpretationNode> getDependentNodes();

    void setDependentNodes(Set<InterpretationNode> nodes);
}
