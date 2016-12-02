package com.nodestand.controllers;

import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.vote.VoteType;
import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VoteController {

    private final Neo4jOperations operations;

    private final UserService userService;

    @Autowired
    public VoteController(Neo4jOperations operations, UserService userService) {
        this.operations = operations;
        this.userService = userService;
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/voteComment")
    public void voteComment(@RequestBody Map<String, Object> params) throws NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();
        User user = operations.load(User.class, userId);

        Long commentId = Long.valueOf((Integer) params.get("commentId"));
        Boolean isUpvote = (Boolean) params.get("isUpvote");

        Comment comment = operations.load(Comment.class, commentId);

//        if (comment.author.getNodeId().equals(user.getNodeId())) {
//            throw new NodeRulesException("Can't upvote your own comment!");
//        }

        user.registerCommentVote(comment, isUpvote);

        operations.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/unvoteComment")
    public void unvoteComment(@RequestBody Map<String, Object> params) throws NodeRulesException {
        Long userId = userService.getUserNodeIdFromSecurityContext();
        User user = operations.load(User.class, userId);

        Long commentId = Long.valueOf((Integer) params.get("commentId"));
        Comment comment = operations.load(Comment.class, commentId);
        user.revokeCommentVote(comment);

        operations.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/voteBody")
    public void voteBody(@RequestBody Map<String, Object> params) throws NodeRulesException {
        Long userId = userService.getUserNodeIdFromSecurityContext();
        User user = operations.load(User.class, userId);

        Long majorVersionId = Long.valueOf((Integer) params.get("majorVersionId"));
        MajorVersion mv = operations.load(MajorVersion.class, majorVersionId);
        String voteTypeStr = (String) params.get("voteType");
        VoteType voteType = VoteType.valueOf(voteTypeStr.toUpperCase());

        user.registerVote(mv, voteType);

        operations.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/unvoteBody")
    public void unvoteBody(@RequestBody Map<String, Object> params) throws NodeRulesException {
        Long userId = userService.getUserNodeIdFromSecurityContext();
        User user = operations.load(User.class, userId);

        Long majorVersionId = Long.valueOf((Integer) params.get("majorVersionId"));
        MajorVersion mv = operations.load(MajorVersion.class, majorVersionId);
        user.revokeVote(mv);

        operations.save(mv);
        operations.save(user);
    }


}
