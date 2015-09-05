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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class PublishController {

    @Autowired
    GraphDao graphDao;

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Autowired
    VersionHelper versionHelper;

    @Autowired
    ArgumentNodeRepository nodeRepository;

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/publishNode")
    public ArgumentNode publishNode(@RequestBody Map<String, Object> params) throws Exception {

        User user = nodeUserDetailsService.getUserFromSession();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));

        ArgumentNode existingNode = nodeRepository.findOne(nodeId);

        if (user.getNodeId() != existingNode.getBody().author.getNodeId()) {
            throw new NotAuthorizedException("Not allowed to publish a draft that you did not create.");
        }

        if (!existingNode.isDraft()) {
            throw new Exception("No new changes to publish!");
        }

        versionHelper.publish(existingNode);

        // TODO: discover whether this node's dependencies have had any updates within their major versions since the
        // draft was originally created. If so, we should give the user the opportunity to bring in the new stuff.
        // However, the default behavior should be to not bring in the new stuff, wary as we are of dummies and vandalism.

        return existingNode;

    }
}
