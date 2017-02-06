package com.nodestand.util;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.LeafNode;
import com.nodestand.nodes.Node;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.source.SourceNode;
import org.springframework.util.CollectionUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class TwoWayUtil {


    public static void updateSupportingNodes(AssertionNode node, Set<Node> children) {
        if (node.getSupportingNodes() != null) {

            for (Node existingChild : node.getSupportingNodes()) {
                if (!children.contains(existingChild)) {
                    existingChild.getDependentNodes().remove(node);
                }
            }
        }

        for (Node newChild : children) {
            if (node.getSupportingNodes() == null || !node.getSupportingNodes().contains(newChild)) {
                if (newChild instanceof AssertionNode) {
                    AssertionNode assertionChild = (AssertionNode) newChild;
                    if (newChild.getDependentNodes() == null) {
                        assertionChild.setDependentNodes(new HashSet<>());
                    }
                    assertionChild.getDependentNodes().add(node);
                } else if (newChild instanceof InterpretationNode) {
                    InterpretationNode interpretationChild = (InterpretationNode) newChild;
                    if (newChild.getDependentNodes() == null) {
                        interpretationChild.setDependentNodes(new HashSet<>());
                    }
                    interpretationChild.getDependentNodes().add(node);
                }
            }
        }

        node.setSupportingNodes(children);
    }

    public static void updateSupportingNodes(InterpretationNode node, SourceNode sourceNode) {

        LeafNode existingSource = node.getSource();
        if (existingSource != null && (sourceNode == null || !Objects.equals(existingSource.getId(), sourceNode.getId()))) {
            existingSource.getDependentNodes().remove(node);

            if (sourceNode != null) {
                if (sourceNode.getDependentNodes() == null) {
                    sourceNode.setDependentNodes(new HashSet<>());
                }
                sourceNode.getDependentNodes().add(node);
            }
        }

        node.setSource(sourceNode);
    }

    public static void forgetBody(ArgumentBody body) {
        if (body.getNode() != null) {
            body.getNode().setBody(null);
        }
    }

    // https://github.com/neo4j/neo4j-ogm/issues/86
    public static void forgetNode(Node node) {

        // Orphan the children
        if (!CollectionUtils.isEmpty(node.getGraphChildren())) {
            if (node instanceof AssertionNode) {
                for (Node supportingNode : ((AssertionNode) node).getSupportingNodes()) {
                    supportingNode.getDependentNodes().remove(node);
                }
            } else if (node instanceof InterpretationNode) {
                ((InterpretationNode) node).getSource().getDependentNodes().remove(node);
            }
        }


        // Notify the parents
        Set<? extends Node> dependentNodes = node.getDependentNodes();
        if (dependentNodes != null) {
            for (Node dependentNode : dependentNodes) {
                if (dependentNode instanceof AssertionNode) {
                    ((AssertionNode)dependentNode).getSupportingNodes().remove(node);
                } else if (dependentNode instanceof InterpretationNode) {
                    ((InterpretationNode)dependentNode).setSource(null);
                }
            }
        }

        // Make the past stop hoping
        if (node.getPreviousVersion() != null) {
            node.getPreviousVersion().getSubsequentVersions().remove(node);
        }

        // Make the future forget
        if (node.getSubsequentVersions() != null) {
            for (Node subsequentVersion : node.getSubsequentVersions()) {
                subsequentVersion.setPreviousVersion(null);
            }
        }

        // Shuffle off mortal coil
        if (node.getBody() != null) {
            node.getBody().setNode(null);
        }
    }
}
