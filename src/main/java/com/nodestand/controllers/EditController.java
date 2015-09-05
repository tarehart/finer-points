package com.nodestand.controllers;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.controllers.serial.EditResult;
import com.nodestand.dao.GraphDao;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.ImmutableNodeException;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.source.SourceBody;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.VersionHelper;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.cypher.internal.compiler.v2_1.planDescription.Argument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.EntityPath;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Objects;

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
    GraphDatabase graphDatabase;

    @Autowired
    Neo4jOperations neo4jOperations;




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
        Long rootId = Long.valueOf((Integer) params.get("rootId"));

        ArgumentNode existingNode = nodeRepository.findOne(nodeId);

        if (existingNode.isDraft()) {
            // We won't need to update any version numbers.
            // Only the original author of the draft is allowed to edit it.

            if (!Objects.equals(user.getNodeId(), existingNode.getBody().author.getNodeId())) {
                throw new NotAuthorizedException("Not allowed to edit a draft that you did not create.");
            }

            doNodeEdits(existingNode, params);

            // Ugh: http://stackoverflow.com/questions/31505729/why-is-my-modified-neo4j-node-property-not-persisted-to-the-db
            neo4jOperations.save(existingNode.getBody());
            nodeRepository.save(existingNode);

            return new EditResult(existingNode);

        } else {

            ArgumentNode draftNode = existingNode.createNewDraft(VersionHelper.startBuild(user), true);

            doNodeEdits(draftNode, params);

            nodeRepository.save(draftNode);

            propagateDraftTowardRoot(draftNode, rootId);

            EditResult result = new EditResult(draftNode);
            result.setGraph(graphDao.getGraph(rootId));

            return result;

        }
    }

    private void doNodeEdits(ArgumentNode node, Map<String, Object> params) throws ImmutableNodeException, NodeRulesException {

        node.getBody().setTitle((String) params.get("title"));

        switch (node.getType()) {
            case "assertion":

                AssertionNode assertionNode = (AssertionNode) node;
                assertionNode.getBody().setBody((String) params.get("body"));

                if (assertionNode.getSupportingNodes() != null) {
                    assertionNode.getSupportingNodes().clear();
                }

                List<Integer> children = (List<Integer>) params.get("links");

                for (Integer id : children) {
                    ArgumentNode supportingNode = nodeRepository.findOne(Long.valueOf(id));
                    if (supportingNode instanceof SourceNode) {
                        throw new NodeRulesException("An assertion node cannot link directly to a source!");
                    }
                    assertionNode.supportedBy(supportingNode);
                }
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

    private void propagateDraftTowardRoot(ArgumentNode draftNode, Long rootId) throws NodeRulesException {

        ArgumentNode preEdit = draftNode.getPreviousVersion();

        Iterable<EntityPath<ArgumentNode, ArgumentNode>> paths = nodeRepository.getPaths(preEdit.getId(), rootId);

        for (EntityPath<ArgumentNode, ArgumentNode> path: paths) {
            ArgumentNode previousNode = draftNode;
            for (Object nodeObj : path.nodeEntities()) {

                ArgumentNode pathNode = (ArgumentNode) nodeObj;

                if (pathNode.getId() == path.startNode().getId()) {
                    continue; // Skip the start node; we've already converted it to a draft.
                }

                // Gotta fork off a new draft version of this published node.
                // The draft version should point to all the same nodes as its
                // previous published incarnation EXCEPT that we will swap in
                // 'previousNode' which is the draft that we created on the
                // previous iteration of the for loop.
                ArgumentNode changeable = pathNode.alterOrCloneToPointToChild(previousNode);

                if (pathNode.getId().equals(changeable.getId())) {
                    // This should save all new nodes because they're linked together
                    nodeRepository.save(changeable);

                    break; // No new node was created, so the upstream link is still valid.
                }

                previousNode = changeable;
            }

            // In case we created new nodes all the way up to the root, make sure we save the root.
            nodeRepository.save(path.endEntity(ArgumentNode.class));
        }
    }
}
