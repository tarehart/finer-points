package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
public class NodeMenuController {

    @Autowired
    ArgumentNodeRepository argumentNodeRepository;

    @Autowired
    UserService userService;

    @Transactional
    @RequestMapping("/rootNodes")
    public Set<ArgumentNode> getRootNodes() {

        return argumentNodeRepository.getRootNodesRich();
    }

    @Transactional
    @RequestMapping("/draftNodes")
    public Set<ArgumentNode> getDraftNodes() {

        Long userId = userService.getUserIdFromSession();
        return argumentNodeRepository.getDraftNodesRich(userId);
    }

    @Transactional
    @RequestMapping("/consumerNodes")
    public Set<ArgumentNode> getConsumerNodes(@RequestParam Long nodeId) {
        Long userId = userService.getUserIdFromSession();
        if (userId != null) {
            return argumentNodeRepository.getConsumerNodes(nodeId, userId);
        } else {
            return argumentNodeRepository.getConsumerNodes(nodeId);
        }
    }

}
