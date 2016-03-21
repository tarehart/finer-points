package com.nodestand.service.argument;

import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.QueryResultModel;
import org.neo4j.ogm.session.Session;
import org.springframework.data.neo4j.template.Neo4jOperations;

import java.util.*;

public class DraftPropagation {


    private static Set<List<ArgumentNode>> resultToNodeLists(Result result, ArgumentNodeRepository argumentRepo) {

        Iterable<Map<String, Object>> results = result.queryResults();

        Set<List<ArgumentNode>> lists = new HashSet<>();

        for (Map<String, Object> r: results) {
            List<ArgumentNode> path = (List<ArgumentNode>) r.get("path"); // Keep in sync with ArgumentNodeRepository.getPaths
            lists.add(path);
        }

        return lists;
    }

    /**
     * This logic once lived in EditController.java
     *
     * @return The id of the root node after propagation
     * @throws NodeRulesException
     */
    static String propagateDraftTowardRoot(ArgumentNode draftNode, QuickGraphResponse graph, Neo4jOperations operations, ArgumentNodeRepository argumentRepo) throws NodeRulesException {

        ArgumentNode preEdit = draftNode.getPreviousVersion();

        ArgumentNode newRoot = null;

        Map<String, ArgumentNode> nodeLookup = new HashMap<>();
        for (ArgumentNode node : graph.getNodes()) {
            nodeLookup.put(node.getStableId(), node);
        }

        Result rawResult = argumentRepo.getPaths(preEdit.getId(), graph.getRootId());

        Set<List<ArgumentNode>> nicePaths = resultToNodeLists(rawResult, argumentRepo);

        for (List<ArgumentNode> path: nicePaths) {

            ArgumentNode priorInPath = draftNode;
            for (ArgumentNode pathNode : path) {

                if (Objects.equals(pathNode.getId(), path.get(0).getId())) {
                    continue; // Skip the start node; we've already converted it to a draft.
                }

                // Gotta fork off a new draft version of this published node.
                // The draft version should point to all the same nodes as its
                // previous published incarnation EXCEPT that we will swap in
                // 'previousNode' which is the draft that we created on the
                // previous iteration of the for loop.

                // TODO: it should also go ahead and get a new minor version, right?

                ArgumentNode previousChild = priorInPath.getPreviousVersion();

                ArgumentNode changeable = pathNode.alterOrCloneToPointToChild(priorInPath, previousChild);

                if (changeable.getGraphChildren().contains(changeable)) {
                    throw new NodeRulesException("Something has gone wrong with publishing and we have a closed loop!");
                }

                // The previous child probably got modified by the alterOrClone operation due to abandonment.
                operations.save(previousChild);
                operations.save(changeable);

                if (pathNode.getId().equals(changeable.getId())) {
                    // There was no clone necessary, it must already have been a draft.
                    // We stop propagation here.
                    // No new node was created, so the upstream link is still valid.
                    break;

                } else if (Objects.equals(pathNode.getId(), path.get(path.size() - 1).getId())) {
                    // We can infer that the root node just got modified!
                    newRoot = changeable;
                }

                priorInPath = changeable;
            }
        }

        if (newRoot != null) {
            return newRoot.getStableId();
        } else {
            return graph.getRootStableId();
        }
    }
}
