package com.nodestand.controllers;

import com.nodestand.nodes.Author;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class UserController {

    private final UserService userService;

    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    @Transactional
    @RequestMapping("/currentUser")
    public Map<String, Object> currentUser() throws NodeRulesException {

        User user = userService.getUserFromSecurityContext();
        if (user == null) {
            return null;
        }

        user = userService.loadUserWithVotes(user.getStableId());

        Map<String, Object> map = new HashMap<>();
        map.put("user", user);
        map.put("bodyVotes", user.getBodyVotes());
        map.put("commentVotes", user.getCommentVoteMap());

        return map;
    }

    @Transactional
    @RequestMapping("/getProfile")
    public Author getProfile(@RequestParam String stableId) throws NodeRulesException {

        return userRepository.loadAuthor(stableId);
    }
}
