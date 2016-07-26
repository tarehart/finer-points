package com.nodestand.controllers;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.service.argument.ArgumentService;
import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class NodeMenuController {

    @Autowired
    ArgumentService argumentService;

    @Autowired
    UserService userService;

    @Transactional
    @RequestMapping("/rootNodes")
    public Set<ArgumentNode> getRootNodes() {

        return argumentService.getRootNodes();
    }

    @Transactional
    @RequestMapping("/draftNodes")
    public Set<ArgumentNode> getDraftNodes() {

        Long userId = userService.getUserNodeIdFromSecurityContext();
        if (userId == null) {
            throw new NotAuthorizedException("Must be logged in to retrieve drafts.");
        }

        return argumentService.getDraftNodes(userId);
    }

    @Transactional
    @RequestMapping("/consumerNodes")
    public Set<ArgumentNode> getConsumerNodes(@RequestParam Long nodeId) {
        Long userId = userService.getUserNodeIdFromSecurityContext();
        if (userId != null) {
            return argumentService.getConsumerNodesIncludingDrafts(userId, nodeId);
        } else {
            return argumentService.getConsumerNodes(nodeId);
        }
    }

}
