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
import com.nodestand.util.BugMitigator;
import org.neo4j.ogm.session.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
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
    Neo4jOperations neo4jOperations;

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

        Result result = neo4jOperations.query("start n=node({id}) " +
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

        Result result = neo4jOperations.query("start n=node({id}) " +
                "match node-[DEFINED_BY]->n " +
                "return max(node.buildVersion) as " + CURRENT_MAX_KEY, params);

        Map<String, Object> resultMap = singleOrNull(result);
        if (resultMap != null) {
            int currentMax = (int) resultMap.get(CURRENT_MAX_KEY);
            return currentMax + 1;
        }

        return 0;
    }

    public void publish(ArgumentNode node) throws NodeRulesException {

        if (!node.getType().equals("source")) {
            // validate that the node and its descendants follow all the rules, e.g. being grounded in sources
            if (hasMissingSupport(node)) {
                throw new NodeRulesException("The roots of this node do not all end in sources!");
            }
            publishDescendantDrafts(node);
        }

        stampVersion(node);
        
        // make new nodes with new version numbers for all consumers. Decorate with the Build object.
        propagateBuild(node);

        // TODO: think about concurrency for setting build numbers.
    }

    /**
     * This used to be done as a transaction with the major version node as a lock. Currently we're being a little
     * unsafe.
     */
    private void stampVersion(ArgumentNode node) {
        ArgumentBody body = node.getBody();
        if (body.getMinorVersion() < 0) {
            //tx.acquireWriteLock(getLockNode(body.getMajorVersion()));
            body.setMinorVersion(getNextMinorVersion(body.getMajorVersion()));
        }
        node.setVersion(getNextBuildVersion(body));
        body.setIsDraft(false);

        // Somehow, when publishing, the author is falling off the body of the node that precedes the node being published.
        // To dupe:
        // Create nodes A -> B -> C
        // Publish A
        // Edit B
        // Edit A
        // Publish A
        // The old A body now no longer has an author.

        bodyRepository.save(node.getBody());
        nodeRepository.save(node);
    }

    private void publishDescendantDrafts(ArgumentNode node) throws NodeRulesException {

        if (node instanceof AssertionNode) {
            AssertionNode assertion = (AssertionNode) node;

            // If assertion.getSupportingNodes returns null, this will go awry. Keep an eye on it.
            neo4jOperations.loadAll(assertion.getSupportingNodes(), 1);

            for (ArgumentNode childNode : assertion.getSupportingNodes()) {
                if (childNode.isDraft()) {
                    publish(childNode);
                }
            }
        } else if (node instanceof InterpretationNode) {
            InterpretationNode interpretation = (InterpretationNode) node;

            // If interpretation.getSource returns null, this will go awry. Keep an eye on it.
            neo4jOperations.load(SourceNode.class, interpretation.getSource().getId());

            if (interpretation.getSource().isDraft()) {
                publish(interpretation.getSource());
            }
        }
    }

    private boolean hasMissingSupport(ArgumentNode node) {
        // TODO: consider making the isDraft attribute belong to nodes instead of bodies so that we can easily
        // short-circuit these queries.

        Map<String, Object> params = new HashMap<>();
        params.put("id", node.getId());

        Result result = neo4jOperations.query(
                "start n=node({id}) match n-[:SUPPORTED_BY*0..]->" +
                        "(support:ArgumentNode) " +
                        "WHERE NOT support-[:INTERPRETS]->(:SourceNode) " +
                        "AND NOT support-[:SUPPORTED_BY]->(:ArgumentNode) " +
                        "return support", params);

        return result.queryResults().iterator().hasNext();
    }

    /**
     * Be careful with this, it does a lot of recursion and hits the database.
     *
     * The question of what nodes should have the build propagated to them, i.e. which trees will be made to incorporate
     * the edit, is a bit complicated. Consider these scenarios:
     *
     * The user has a draft A which points to an existing node B, and they have edited the existing node B, and they are now
     * publishing that edit of B. The draft A should be included in the build, but any consumers of A should not.
     *
     *
     *
     * Note for later: when rendering the graph of a major version node, we pick the best argument node within it and
     * run with it. Best is defined as: the most recently published node which is a direct iteration on the previous
     * best node. To implement this, we can mark a node as the 'tip' and have that tip marking pass on to qualifying
     * changes.
     *
     *
     *
     * New idea: If you are editing a non-tip node, maybe we should automatically make that a new major version.
     *
     * Possible confusion: A user edits a particular node, then goes to a different tree and sees a node with the same
     * title but slightly different content. Should they edit that too? It would be preferable to look at the parent and
     * see a button to "point it to my version instead of this current one."
     *
     * Maybe there should not be a special tip node because that could contribute to edit wars.
     *
     * TODO: instead of making repeated reads from the database, select out the entire consumer tree first and then
     * operate on that.
     *
     * @param updatedNode
     */
    private void propagateBuild(ArgumentNode updatedNode) throws NodeRulesException {

        if (updatedNode.getPreviousVersion() == null) {
            return;
        }

        Set<ArgumentNode> consumers = new HashSet<>();
        addIfNotNull(updatedNode.getDependentNodes(), consumers);

        for (ArgumentNode consumer: consumers) {

            BugMitigator.loadArgumentNode(neo4jOperations, consumer.getId(), 1); // Flesh out the properties

            // Don't mess with drafts.
            // 1. Might be a draft that already points to updatedNode. No action required.
            // 2. Might belong to somebody else. Design decision to not mess with that.
            // 3. Might be the authors draft. Still a design decision to not mess with that.
            if (!consumer.isDraft()) {
                ArgumentNode updatedConsumer = consumer.alterOrCloneToPointToChild(updatedNode);
                if (updatedConsumer.getGraphChildren().contains(updatedConsumer)) {
                    throw new NodeRulesException("Something has gone wrong with publishing and we have a closed loop!");
                }
                updatedConsumer.setVersion(getNextBuildVersion(updatedConsumer.getBody()));
                bodyRepository.save(updatedConsumer.getBody());
                nodeRepository.save(updatedConsumer);

                propagateBuild(updatedConsumer);
            }
        }
    }

    /**
     * Yes, I know about polymorphism. I'm declining to use it here because the various getDependentNodes methods have
     * special annotations related to spring data neo4j. I could have a polymorphic wrapper, but this is actually cleaner.
     */
//    private Set<ArgumentNode> getConsumers(ArgumentNode updatedNode) {
//        Set<ArgumentNode> consumers = new HashSet<>();
//        if (updatedNode instanceof  AssertionNode) {
//            addIfNotNull(((AssertionNode) updatedNode).getDependentNodes(), consumers);
//        } else if (updatedNode instanceof  InterpretationNode) {
//            addIfNotNull(((InterpretationNode) updatedNode).getDependentNodes(), consumers);
//        } else if (updatedNode instanceof  SourceNode) {
//            addIfNotNull(((SourceNode) updatedNode).getDependentNodes(), consumers);
//        }
//        return consumers;
//    }

    private void addIfNotNull(Set<? extends ArgumentNode> values, Set<ArgumentNode> collection) {
        if (values != null) {
            collection.addAll(values);
        }
    }
}
