package com.nodestand.controllers;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.dao.GraphDao;
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
    @RequestMapping("/publishassertion")
    public AssertionNode publishAssertion(@RequestBody Long assertionNodeId) throws Exception {

        User user = nodeUserDetailsService.getUserFromSession();

        AssertionNode existingNode = (AssertionNode) nodeRepository.findOne(assertionNodeId);

        if (user.getNodeId() != existingNode.getBody().author.getNodeId()) {
            throw new NotAuthorizedException("Not allowed to publish a draft that you did not create.");
        }

        if (!existingNode.getBody().isDraft()) {
            throw new Exception("No new changes to publish!");
        }

        versionHelper.publish(existingNode);

        return existingNode;

    }
}
