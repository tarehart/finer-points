package com.nodestand.service.argument;

import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.neo4j.ogm.session.Session;

import java.util.*;

public class DraftPropagation {

    /**
     * This logic once lived in EditController.java
     *
     * @return The id of the root node after propagation
     * @throws NodeRulesException
     */
    static String propagateDraftTowardRoot(ArgumentNode draftNode, QuickGraphResponse graph, Session session, ArgumentNodeRepository argumentRepo) throws NodeRulesException {

        ArgumentNode preEdit = draftNode.getPreviousVersion();

        ArgumentNode newRoot = null;

        Map<String, ArgumentNode> nodeLookup = new HashMap<>();
        for (ArgumentNode node : graph.getNodes()) {
            nodeLookup.put(node.getStableId(), node);
        }

        Iterable<Map<String, Object>> paths = argumentRepo.getPaths(preEdit.getId(), graph.getRootId());

        Set<List<ArgumentNode>> nicePaths = new HashSet<>();

        for (Map<String, Object> iterPath: paths) {
            List<ArgumentNode> nicePath = new LinkedList<>();
            List<Map<String, Object>> roughPath = (List<Map<String, Object>>) iterPath.get("p");
            for (int i = 0; i < roughPath.size(); i += 2) {
                String stableId = (String) roughPath.get(i).get("stableId");
                ArgumentNode hashedNode = nodeLookup.get(stableId);
                nicePath.add(hashedNode);
            }
            nicePaths.add(nicePath);
        }

        for (List<ArgumentNode> path: nicePaths) {

            ArgumentNode priorInPath = draftNode;
            for (Object nodeObj : path) {

                ArgumentNode pathNode = (ArgumentNode) nodeObj;

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

                // The previous child probably got modified by the alterOrClone operation due to abandonment.
                session.save(previousChild);

                if (changeable.getGraphChildren().contains(changeable)) {
                    throw new NodeRulesException("Something has gone wrong with publishing and we have a closed loop!");
                }

                if (pathNode.getId().equals(changeable.getId())) {
                    // There was no clone necessary, it must already have been a draft.
                    // We stop propagation here.

                    // This should save all new nodes because they're linked together
                    session.save(changeable);

                    break; // No new node was created, so the upstream link is still valid.

                } else if (Objects.equals(pathNode.getId(), path.get(path.size() - 1).getId())) {
                    // We can infer that the root node just got modified!
                    session.save(changeable);
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
