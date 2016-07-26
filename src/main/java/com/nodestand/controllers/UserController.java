package com.nodestand.controllers;

import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    Neo4jOperations operations;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/currentUser")
    public Map<String, Object> currentUser() throws NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();
        if (userId == null) {
            return null;
        }
        User user = operations.load(User.class, userId);

        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("bodyVotes", user.getBodyVotes());
        map.put("commentVotes", user.getCommentVoteMap());

        return map;
    }

    @Transactional
    @RequestMapping("/getProfile")
    public User getProfile(@RequestParam String stableId) throws NodeRulesException {

        return userRepository.getUser(stableId);
    }
}
