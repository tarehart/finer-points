package com.nodestand.controllers;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.controllers.serial.EditResult;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.dao.GraphDao;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.ImmutableNodeException;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentBodyRepository;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.VersionHelper;
import com.nodestand.service.NodeUserDetailsService;
import com.nodestand.util.BugMitigator;
import com.nodestand.util.TwoWayUtil;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class EditController {

    @Autowired
    GraphDao graphDao;

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Autowired
    VersionHelper versionHelper;

    @Autowired
    ArgumentNodeRepository nodeRepository;

    @Autowired
    ArgumentBodyRepository bodyRepository;

    @Autowired
    Session session;




    /**
     * For now, this will always mark the newly created node as a draft. There will be a separate operation
     * called 'publish' which will impose more rules.
     *
     * Hyperlinks within body text have an id corresponding to a major version. That way we don't have to update them
     * when children are directly edited and thereby have their minor versions changed. The real links are managed by
     * the graph database, where nodes (not bodies) link to other nodes (which are at the build version level). Giving
     * they hyperlinks the node id of the major version will be sufficient to map to the correct child node.
     *
     * - We do not want to create multiple minor versions as people make draft edits; that should only happen after
     * publishing.
     * - Can we just say that draft-mode edits don't do anything at all to the version number?
     *
     *
     * @return
     * @throws NotAuthorizedException
     * @throws ImmutableNodeException
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editAssertion")
    public EditResult editAssertion(@RequestBody Map<String, Object> params) throws NotAuthorizedException, ImmutableNodeException, NodeRulesException {
        return editNode(params);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editInterpretation")
    public EditResult editInterpretation(@RequestBody Map<String, Object> params) throws NotAuthorizedException, ImmutableNodeException, NodeRulesException {
        return editNode(params);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editSource")
    public EditResult editSource(@RequestBody Map<String, Object> params) throws NotAuthorizedException, ImmutableNodeException, NodeRulesException {
        return editNode(params);
    }

    private EditResult editNode(Map<String, Object> params) throws NotAuthorizedException, NodeRulesException, ImmutableNodeException {

        User user = nodeUserDetailsService.getUserFromSession();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));
        String rootStableId = (String) params.get("rootStableId");

        ArgumentNode existingNode = BugMitigator.loadArgumentNode(session, nodeId, 2);


        if (!existingNode.getBody().isEditable()) {
            throw new NodeRulesException("Cannot edit this node!");
        }

        if (existingNode.getBody().isPublic()) {

            // Create a new draft version

            // This will set the previous version on the draft. Later, when we publish the edit,
            // this draft will copy its contents to the previous version and then be destroyed.
            ArgumentNode draftNode = existingNode.createNewDraft(VersionHelper.startBuild(user), true);

            doNodeEdits(draftNode, user, params);

            session.save(draftNode);

            String newRootStableId = null;
            if (existingNode.getStableId().equals(rootStableId)) {
                newRootStableId = draftNode.getStableId();
            } else {
                newRootStableId = propagateDraftTowardRoot(draftNode, rootStableId, session);
            }

            EditResult result = new EditResult(draftNode);
            result.setGraph(graphDao.getGraph(newRootStableId));

            return result;

        } else {

            doNodeEdits(existingNode, user, params);

            session.save(existingNode);

            return new EditResult(existingNode);


        }
    }

    private void doNodeEdits(ArgumentNode node, User author, Map<String, Object> params) throws ImmutableNodeException, NodeRulesException {

        node.getBody().setTitle((String) params.get("title"));

        switch (node.getType()) {
            case "assertion":

                AssertionNode assertionNode = (AssertionNode) node;
                assertionNode.getBody().setBody((String) params.get("body"));

                Set<ArgumentNode> children = new HashSet<>();
                for (Integer id : (List<Integer>) params.get("links")) {
                    ArgumentNode supportingNode = nodeRepository.findOne(Long.valueOf(id));
                    if (supportingNode instanceof SourceNode) {
                        throw new NodeRulesException("An assertion node cannot link directly to a source!");
                    }
                    children.add(supportingNode);
                }

                TwoWayUtil.updateSupportingNodes(assertionNode, children);

                break;
            case "interpretation":
                InterpretationNode interpretationNode = (InterpretationNode) node;
                interpretationNode.getBody().setBody((String) params.get("body"));

                try {
                    if (params.get("sourceId") != null) {
                        Long sourceId = Long.valueOf((Integer) params.get("sourceId"));
                        SourceNode source = (SourceNode) nodeRepository.findOne(sourceId);
                        interpretationNode.setSource(source);
                    }
                } catch (ClassCastException e) {
                    throw new NodeRulesException("An interpretation node can only link to a source node!");
                }
                break;
            case "source":
                SourceNode sourceNode = (SourceNode) node;
                sourceNode.getBody().setUrl((String) params.get("url"));
                break;
        }
    }

    /**
     *
     * @return The id of the root node after propagation
     * @throws NodeRulesException
     */
    private String propagateDraftTowardRoot(ArgumentNode draftNode, String rootStableId, Session session) throws NodeRulesException {

        ArgumentNode preEdit = draftNode.getPreviousVersion();

        ArgumentNode newRoot = null;

        QuickGraphResponse graph = graphDao.getGraph(rootStableId);

        Map<String, ArgumentNode> nodeLookup = new HashMap<>();
        for (ArgumentNode node : graph.getNodes()) {
            nodeLookup.put(node.getStableId(), node);
        }

        Iterable<Map<String, Object>> paths = nodeRepository.getPaths(preEdit.getId(), graph.getRootId());

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

            ArgumentNode previousNode = draftNode;
            for (Object nodeObj : path) {

                ArgumentNode pathNode = (ArgumentNode) nodeObj;

                if (pathNode.getId() == path.get(0).getId()) {
                    continue; // Skip the start node; we've already converted it to a draft.
                }

                // Gotta fork off a new draft version of this published node.
                // The draft version should point to all the same nodes as its
                // previous published incarnation EXCEPT that we will swap in
                // 'previousNode' which is the draft that we created on the
                // previous iteration of the for loop.

                // TODO: it should also go ahead and get a new minor version, right?
                ArgumentNode changeable = pathNode.alterOrCloneToPointToChild(previousNode, previousNode.getPreviousVersion());

                if (changeable.getGraphChildren().contains(changeable)) {
                    throw new NodeRulesException("Something has gone wrong with publishing and we have a closed loop!");
                }

                if (pathNode.getId().equals(changeable.getId())) {
                    // There was no clone necessary, it must already have been a draft.
                    // We stop propagation here.

                    // This should save all new nodes because they're linked together
                    session.save(changeable);

                    break; // No new node was created, so the upstream link is still valid.

                } else if (pathNode.getId() == path.get(path.size() - 1).getId()) {
                    // We can infer that the root node just got modified!
                    session.save(changeable);
                    newRoot = changeable;
                }

                previousNode = changeable;
            }
        }

        if (newRoot != null) {
            return newRoot.getStableId();
        } else {
            return rootStableId;
        }
    }
}