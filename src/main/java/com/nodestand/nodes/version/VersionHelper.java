package com.nodestand.nodes.version;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentBodyRepository;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.util.TwoWayUtil;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class VersionHelper {

    private static final String CURRENT_MAX_KEY = "currentMax";

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
            MajorVersion mv = new MajorVersion(1, new VersionAggregator());
            body.setVersion(mv, 0);
        } else {
            // A minor version of -1 is set for all drafts. It will receive a real version number later
            // when it is published.
            body.setMinorVersion(-1);
        }


    }

    public static Build startBuild(User author) {
        Build build = new Build();
        build.author = author;
        return build;
    }

    private int getNextMinorVersion(MajorVersion majorVersion) {
        Map<String, Object> params = new HashMap<>();
        params.put( "id", majorVersion.id );

        Result result = session.query("start n=node({id}) " +
                "match body-[VERSION_OF]->n " +
                "return max(body.minorVersion) as " + CURRENT_MAX_KEY, params);

        Map<String, Object> resultMap = singleOrNull(result);
        if (resultMap != null) {
            int currentMax = (int) resultMap.get(CURRENT_MAX_KEY);
            return currentMax + 1;
        }

        return 0;
    }

    private Map<String, Object> singleOrNull(Result result) {
        Iterator<Map<String, Object>> it = result.queryResults().iterator();
        if (it.hasNext()) {
            return it.next();
        }
        return null;
    }

    private int getNextBuildVersion(ArgumentBody body) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", body.getId());

        Result result = session.query("start n=node({id}) " +
                "match node-[DEFINED_BY]->n " +
                "return max(node.buildVersion) as " + CURRENT_MAX_KEY, params);

        Map<String, Object> resultMap = singleOrNull(result);
        if (resultMap != null) {
            int currentMax = (int) resultMap.get(CURRENT_MAX_KEY);
            return currentMax + 1;
        }

        return 0;
    }

    public ArgumentNode publish(ArgumentNode node) throws NodeRulesException {
        if (!node.getType().equals("source")) {
            // validate that the node and its descendants follow all the rules, e.g. being grounded in sources
            if (hasMissingSupport(node)) {
                throw new NodeRulesException("The roots of this node do not all end in sources!");
            }
            publishDescendants(node);
        }

        ArgumentNode resultingNode = node;

        ArgumentNode previousVersion = node.getPreviousVersion();
        if (previousVersion != null && !previousVersion.isFinalized()) {
            // previous version is the edit target.

            // Copy everything over into the previous version
            node.copyContentTo(previousVersion);
            node.getBody().applyEditTo(previousVersion.getBody());

            resultingNode = previousVersion;

            // Any parents that had pointed to the draft should be modified so that they point to the
            // published version. The draft is going away.
            Set<? extends ArgumentNode> dependentNodes = node.getDependentNodes();
            if (dependentNodes != null) {
                for (ArgumentNode parent : node.getDependentNodes()) {
                    parent.alterToPointToChild(resultingNode, node);
                }
            }

            // Destroy the current version
            session.delete(node.getBuild());
            session.delete(node.getBody());
            session.delete(node);

            TwoWayUtil.forgetBody(node.getBody());
            TwoWayUtil.forgetNode(node);
        }

        ArgumentBody body = resultingNode.getBody();
        if (body.getMinorVersion() < 0) {
            body.setMinorVersion(getNextMinorVersion(body.getMajorVersion()));
        }

        body.setIsPublic(true);

        session.save(resultingNode);

        return resultingNode;
    }

    private void publishDescendants(ArgumentNode node) throws NodeRulesException {

        if (node instanceof AssertionNode) {
            AssertionNode assertion = (AssertionNode) node;

            // If assertion.getSupportingNodes returns null, this will go awry. Keep an eye on it.
            session.loadAll(assertion.getSupportingNodes(), 1);

            for (ArgumentNode childNode : assertion.getSupportingNodes()) {
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



    public void snapshot(ArgumentNode node, Build build) throws NodeRulesException {

        if (!node.getType().equals("source")) {
            // validate that the node and its descendants follow all the rules, e.g. being grounded in sources
            if (hasMissingSupport(node)) {
                throw new NodeRulesException("The roots of this node do not all end in sources!");
            }
        }

        snapshotHelper(node, build);
    }

    private ArgumentNode snapshotHelper(ArgumentNode node, Build build) throws NodeRulesException {
        if (node.isFinalized()) {
            return node;
        }

        // TODO: make some effort to dedupe with existing snapshots, in terms of both nodes and bodies.
        ArgumentNode clone = node.createNewDraft(build, true);

        snapshotDescendants(clone, build);

        ArgumentBody body = clone.getBody();

        clone.setVersion(getNextBuildVersion(body));
        body.setIsEditable(false);

        session.save(clone);

        return clone;
    }

    /**
     * Assumes node has already been cloned. Will replace node's descendants with snapshots.
     */
    private void snapshotDescendants(ArgumentNode node, Build build) throws NodeRulesException {

        if (node instanceof AssertionNode) {
            AssertionNode assertion = (AssertionNode) node;

            // If assertion.getSupportingNodes returns null, this will go awry. Keep an eye on it.
            session.loadAll(assertion.getSupportingNodes(), 1);

            Set<ArgumentNode> snappedDescendants = new HashSet<>();

            for (ArgumentNode childNode : assertion.getSupportingNodes()) {
                if (!childNode.isFinalized()) {
                    snappedDescendants.add(snapshotHelper(childNode, build));
                }
            }

            assertion.setSupportingNodes(snappedDescendants);
        } else if (node instanceof InterpretationNode) {
            InterpretationNode interpretation = (InterpretationNode) node;

            // If interpretation.getSource returns null, this will go awry. Keep an eye on it.
            session.load(SourceNode.class, interpretation.getSource().getId());

            if (!interpretation.getSource().isFinalized()) {

                interpretation.setSource((SourceNode) snapshotHelper(interpretation.getSource(), build));
            }
        }

    }

    private boolean hasMissingSupport(ArgumentNode node) {
        // TODO: consider making the isFinalized attribute belong to nodes instead of bodies so that we can easily
        // short-circuit these queries.

        Map<String, Object> params = new HashMap<>();
        params.put("id", node.getId());

        Result result = session.query(
                "start n=node({id}) match n-[:SUPPORTED_BY*0..]->" +
                        "(support:ArgumentNode) " +
                        "WHERE NOT support-[:INTERPRETS]->(:SourceNode) " +
                        "AND NOT support-[:SUPPORTED_BY]->(:ArgumentNode) " +
                        "return support", params);

        return result.queryResults().iterator().hasNext();
    }
}
