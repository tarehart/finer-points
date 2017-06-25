package com.nodestand.controllers;

import com.nodestand.nodes.Author;
import com.nodestand.nodes.ForbiddenNodeOperationException;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.service.user.UserService;
import com.nodestand.service.vote.ScoreLog;
import com.nodestand.service.vote.ScoreLogReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class UserController {

    private final UserService userService;

    private final UserRepository userRepository;

    private final ScoreLogReader scoreLogReader;

    @Autowired
    public UserController(UserService userService, UserRepository userRepository, ScoreLogReader scoreLogReader) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.scoreLogReader = scoreLogReader;
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

    @RequestMapping("/getScoreLog")
    public List<ScoreLog> getScoreLog(@RequestParam String stableId) throws NodeRulesException {

        List<ScoreLog> scoreLogForUser = scoreLogReader.getScoreLogForUser(stableId);

        return scoreLogForUser;
    }

    @RequestMapping("/canChangeAuthorName")
    public boolean canChangeAuthorName(@RequestParam String authorStableId) throws NodeRulesException {

        return !userService.isPublishedAuthor(authorStableId);
    }

    @RequestMapping("/changeAuthorName")
    public Author changeAuthorName(@RequestParam String authorStableId, @RequestParam String authorName) throws NodeRulesException {

        if (!canChangeAuthorName(authorStableId)) {
            throw new ForbiddenNodeOperationException("Not allowed to change the name of this author!");
        }

        return userService.changeAuthorName(authorStableId, authorName);
    }
}
