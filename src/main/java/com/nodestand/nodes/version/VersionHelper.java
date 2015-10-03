package com.nodestand.nodes.version;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.data.neo4j.transaction.Neo4jTransactionManager;
import org.springframework.stereotype.Component;
import org.neo4j.ogm.session.result.Result;

import java.util.*;

@Component
public class VersionHelper {

    private static final String CURRENT_MAX_KEY = "currentMax";

    @Autowired
    GraphDatabase graphDatabase;

    @Autowired
    ArgumentNodeRepository nodeRepository;

    @Autowired
    Neo4jOperations neo4jOperations;

    @Autowired
    Neo4jTemplate neo4jTemplate;

    @Autowired
    Neo4jTransactionManager neo4jTransactionManager;

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

        Result result = neo4jTemplate.query("start n=node({id}) " +
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
        params.put( "id", body.getId() );

        Result result = neo4jTemplate.query("start n=node({id}) " +
                "match node-[DEFINED_BY]->n " +
                "return max(node.buildVersion) as " + CURRENT_MAX_KEY, params);

        Map<String, Object> resultMap = singleOrNull(result);
        if (resultMap != null) {
            int currentMax = (int) resultMap.get(CURRENT_MAX_KEY);
            return currentMax + 1;
        }

        return 0;
    }

    private Node getLockNode(MajorVersion mv) {
        return graphDatabase.getNodeById(mv.getId());
    }

    public void publish(ArgumentNode node) throws NodeRulesException {

        if (!node.getType().equals("source")) {
            // validate that the node and its descendants follow all the rules, e.g. being grounded in sources
            if (hasMissingSupport(node)) {
                throw new NodeRulesException("The roots of this node do not all end in sources!");
            }
            publishDescendantDrafts(node);
        }


        try ( Transaction tx = graphDatabase.beginTx() ) {
            ArgumentBody body = node.getBody();
            if (body.getMinorVersion() < 0) {
                tx.acquireWriteLock(getLockNode(body.getMajorVersion()));
                body.setMinorVersion(getNextMinorVersion(body.getMajorVersion()));
            }
            node.setVersion(0);
            body.setIsDraft(false);
            neo4jOperations.save(node.getBody());
            nodeRepository.save(node);
            tx.success();
        }
        
        // make new nodes with new version numbers for all consumers. Decorate with the Build object.
        propagateBuild(node);

        // TODO: think about concurrency for setting build numbers.
    }

    private void publishDescendantDrafts(ArgumentNode node) throws NodeRulesException {

        if (node instanceof AssertionNode) {
            AssertionNode assertion = (AssertionNode) node;

            // If assertion.getSupportingNodes returns null, this will go awry. Keep an eye on it.
            neo4jTemplate.fetch(assertion.getSupportingNodes());

            for (ArgumentNode childNode : assertion.getSupportingNodes()) {
                if (childNode.isDraft()) {
                    publish(childNode);
                }
            }
        } else if (node instanceof InterpretationNode) {
            InterpretationNode interpretation = (InterpretationNode) node;

            // If interpretation.getSource returns null, this will go awry. Keep an eye on it.
            neo4jTemplate.fetch(interpretation.getSource());

            if (interpretation.getSource().isDraft()) {
                publish(interpretation.getSource());
            }
        }
    }

    private boolean hasMissingSupport(ArgumentNode node) {
        // TODO: consider making the isDraft attribute belong to nodes instead of bodies so that we can easily
        // short-circuit these queries.

        Map<String, Object> params = new HashMap<>();
        params.put( "id", node.getId() );

        Result result = neo4jTemplate.query(
                "start n=node({id}) match n-[:SUPPORTED_BY*0..]->" +
                        "(support:ArgumentNode) " +
                        "WHERE NOT support-[:INTERPRETS]->(:SourceNode) " +
                        "AND NOT support-[:SUPPORTED_BY]->(:ArgumentNode) " +
                        "return support", params);

        return result.singleOrNull() != null;
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

        Map<String, Object> params = new HashMap<>();
        params.put( "id", updatedNode.getPreviousVersion().getId() );

        Result result = neo4jTemplate.query("start n=node({id}) " +
                "match consumer-[:SUPPORTED_BY|INTERPRETS]->n return consumer", params);


        List<ArgumentNode> consumers = new LinkedList<>();

        for (Map<String, Object> map: result) {
            consumers.add(neo4jOperations.convert(map.get("consumer"), ArgumentNode.class));
        }
        result.finish();

        for (ArgumentNode consumer: consumers) {

            // TODO: if the consumer is a draft, we don't really need to copy it, just
            // swap out the descendant.
            ArgumentNode updatedConsumer = consumer.alterOrCloneToPointToChild(updatedNode);
            updatedConsumer.setVersion(getNextBuildVersion(updatedConsumer.getBody()));
            nodeRepository.save(updatedConsumer);

            propagateBuild(updatedConsumer);

        }
    }
}
