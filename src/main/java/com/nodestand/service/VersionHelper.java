package com.nodestand.service;

import com.nodestand.nodes.*;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentBodyRepository;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.version.VersionAggregator;
import com.nodestand.util.TwoWayUtil;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VersionHelper {

    @Autowired
    ArgumentNodeRepository nodeRepository;

    @Autowired
    ArgumentBodyRepository bodyRepository;

    @Autowired
    Session session;

    /**
     * This sets the major and minor version on the draft body.
     * @param body
     * @return
     */
    public static void decorateDraftBody(ArgumentBody body) {

        if (body.getMajorVersion() == null) {
            // If the major version is null, we assume this is a brand new body, i.e. version 1.0.0.
            // Normally we would wait until publish to set the version numbers, but we already know what they are.
            // Doing it eagerly because why not.
            MajorVersion mv = new MajorVersion(1, new VersionAggregator(), body.author);
            body.setVersion(mv, 0);
        } else {
            // A minor version of -1 is set for all drafts. It will receive a real version number later
            // when it is published.
            body.setMinorVersion(-1);
        }


    }

    private int getNextMinorVersion(MajorVersion majorVersion) {

        Integer max = nodeRepository.getMaxMinorVersion(majorVersion.getId());

        if (max != null) {
            return max + 1;
        }

        return 0;
    }

    public Node publish(Node draftNode) throws NodeRulesException {

        // We'll need the reference to the major version later.
        nodeRepository.loadWithMajorVersion(draftNode.getId());

        if (!(draftNode instanceof LeafNode)) {
            // validate that the node and its descendants follow all the rules, e.g. being grounded in sources
            if (hasMissingSupport(draftNode)) {
                throw new NodeRulesException("The roots of this node do not all end in sources!");
            }
            publishDescendants(draftNode);
        }

        Node resultingNode = draftNode;

        Node publicVersion = draftNode.getPreviousVersion();
        if (publicVersion != null && !publicVersion.isFinalized()) {
            // previous version is the edit target.

            // Make sure it's loaded
            if (publicVersion.getBody() == null) {
                session.load(publicVersion.getClass(), publicVersion.getId(), 1);
            }

            // Copy links to children into the previous version
            draftNode.copyContentTo(publicVersion);

            bodyTransplant(draftNode, publicVersion);

            ArgumentBody freshlyPublishedBody = publicVersion.getBody();
            freshlyPublishedBody.setDateEdited(new Date());
            freshlyPublishedBody.setIsPublic(true);

            // Any parents that had pointed to the draft should be modified so that they point to the
            // published version. The draft is going away. We will save them later when the dust has settled a bit.
            Set<Node> dependentNodes = new HashSet<>();
            if (draftNode.getDependentNodes() != null) {
                dependentNodes.addAll(draftNode.getDependentNodes());
                for (Node parent : dependentNodes) {
                    parent.alterToPointToChild(publicVersion, draftNode);
                }
            }

            // Destroy the current version
            session.delete(draftNode);

            TwoWayUtil.forgetNode(draftNode);

            for (Node parent : dependentNodes) {
                if (parent instanceof AssertionNode) {
                    // This is probably unnecessary, I'm doing it just to be safe.
                    // Consider removing this later for performance reasons.
                    ((AssertionNode)parent).updateChildOrder(nodeRepository);
                }
                session.save(parent);
            }

            resultingNode = publicVersion;
        }

        ArgumentBody body = resultingNode.getBody();
        if (body.getMinorVersion() < 0) {
            body.setMinorVersion(getNextMinorVersion(body.getMajorVersion()));
        }

        body.setIsPublic(true);

        if (resultingNode instanceof AssertionNode) {
            // This is probably unnecessary, I'm doing it just to be safe.
            // Consider removing this later for performance reasons.
            ((AssertionNode)resultingNode).updateChildOrder(nodeRepository);
        }
        session.save(resultingNode);

        return resultingNode;
    }

    private void bodyTransplant(Node donor, Node recipient) {
        // Null out the back-references
        recipient.getBody().setNode(null);
        donor.getBody().setNode(null);

        // Move the body
        recipient.setBody(donor.getBody());

        // Reciprocate the relationship
        donor.getBody().setNode(recipient);
    }

    private void publishDescendants(Node node) throws NodeRulesException {

        if (node instanceof AssertionNode) {
            AssertionNode assertion = (AssertionNode) node;

            // If assertion.getSupportingNodes returns null, this will go awry. Keep an eye on it.
            Collection<ArgumentNode> support = session.loadAll(ArgumentNode.class,
                    assertion.getSupportingNodes().stream().map(Node::getId).collect(Collectors.toList()), 1);

            for (ArgumentNode childNode : support) {
                if (!childNode.getBody().isPublic()) {
                    publish(childNode);
                }
            }
        } else if (node instanceof InterpretationNode) {
            InterpretationNode interpretation = (InterpretationNode) node;

            // If interpretation.getSource returns null, this will go awry. Keep an eye on it.
            session.load(SourceNode.class, interpretation.getSource().getId());

            if (!interpretation.getSource().getBody().isPublic()) {
                publish(interpretation.getSource());
            }
        }
    }

    private boolean hasMissingSupport(Node node) {
        // TODO: consider making the isFinalized attribute belong to nodes instead of bodies so that we can easily
        // short-circuit these queries.

        Set<ArgumentNode> unsupportedNodes = nodeRepository.getUnsupportedNodes(node.getId());

        return !unsupportedNodes.isEmpty();
    }
}
