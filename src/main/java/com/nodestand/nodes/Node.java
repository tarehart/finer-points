package com.nodestand.nodes;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Collection;
import java.util.Set;

@NodeEntity
public interface Node {
    Long getId();
    String getStableId();
    ArgumentBody getBody();
    void setBody(ArgumentBody body);
    boolean isFinalized();
    Set<? extends Node> getDependentNodes();
    Node getPreviousVersion();
    void alterToPointToChild(Node replacementChild, Node existingChildNode) throws NodeRulesException;

    /**
     * This should be usable in a scenario where this node is a temporary repository of user edits destined for
     * the target node. It should not muck around with any metadata, just user-editable stuff.
     */
    void copyContentTo(Node target) throws NodeRulesException;

    Set<Node> getGraphChildren();

    Set<Node> getSubsequentVersions();

    void setPreviousVersion(Node node);
}
