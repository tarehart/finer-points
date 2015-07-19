package com.nodestand.controllers;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.dao.GraphDao;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.ImmutableNodeException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.version.VersionHelper;
import com.nodestand.service.NodeUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * @param assertionNodeId
     * @param title
     * @param body
     * @param children A list of argument node ids that the user wants as children.
     * @return
     * @throws NotAuthorizedException
     * @throws ImmutableNodeException
     */
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editassertion")
    public AssertionNode editAssertion(Long assertionNodeId, String title, String body, List<Long> children) throws NotAuthorizedException, ImmutableNodeException {

        User user = nodeUserDetailsService.getUserFromSession();

        AssertionNode existingNode = (AssertionNode) nodeRepository.findOne(assertionNodeId);

        if (existingNode.getBody().isDraft()) {
            // We won't need to update any version numbers.
            // Only the original author of the draft is allowed to edit it.

            if (user.getNodeId() != existingNode.getBody().author.getNodeId()) {
                throw new NotAuthorizedException("Not allowed to edit a draft that you did not create.");
            }

            existingNode.getBody().setTitle(title);
            existingNode.getBody().setBody(body);

            existingNode.setSupportingNodes(null);

            for (Long id : children) {
                ArgumentNode supportingNode = nodeRepository.findOne(id);
                existingNode.supportedBy(supportingNode);
            }

            nodeRepository.save(existingNode);

            return null;

        } else {

            AssertionBody newBodyVersion = new AssertionBody(title, body, user);
            newBodyVersion.setMajorVersion(existingNode.getBody().getMajorVersion()); // Same major version. Jumping to new major version will be a separate operation.
            newBodyVersion.setMinorVersion(-1); // It's a draft, we don't know the minor version yet. Deferred until publish.

            AssertionNode draftNode = newBodyVersion.constructNode(versionHelper);

            for (Long id : children) {
                ArgumentNode supportingNode = nodeRepository.findOne(id);
                existingNode.supportedBy(supportingNode);
            }

            nodeRepository.save(draftNode);

            return draftNode;
        }
    }
}
