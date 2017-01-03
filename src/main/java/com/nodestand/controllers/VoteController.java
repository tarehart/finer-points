package com.nodestand.controllers;

import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.vote.ArgumentVote;
import com.nodestand.nodes.vote.VoteType;
import com.nodestand.service.user.UserService;
import com.nodestand.service.vote.VoteService;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
public class VoteController {

    private final Session session;

    private final UserService userService;

    private final VoteService voteService;

    @Autowired
    public VoteController(Session session, UserService userService, VoteService voteService) {
        this.session = session;
        this.userService = userService;
        this.voteService = voteService;
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/voteComment")
    public void voteComment(@RequestBody Map<String, Object> params) throws NodeRulesException {

        User user = userService.getUserFromSecurityContext();

        Long commentId = Long.valueOf((Integer) params.get("commentId"));
        Boolean isUpvote = (Boolean) params.get("isUpvote");

        Comment comment = session.load(Comment.class, commentId);

        if (user.getAliases().stream().anyMatch(a -> a.getStableId().equals(comment.author.getStableId()))) {
            throw new NodeRulesException("Can't upvote your own comment!");
        }

        user.registerCommentVote(comment, isUpvote);

        session.save(comment);
        session.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/unvoteComment")
    public void unvoteComment(@RequestBody Map<String, Object> params) throws NodeRulesException {
        Long userId = userService.getUserNodeIdFromSecurityContext();
        User user = session.load(User.class, userId);

        Long commentId = Long.valueOf((Integer) params.get("commentId"));
        Comment comment = session.load(Comment.class, commentId);
        user.revokeCommentVote(comment);

        session.save(comment);
        session.save(user);
    }


    public static class VoteBodyInput {
        public String nodeStableId;
        public String voteType;
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/voteBody")
    public void voteBody(@RequestBody VoteBodyInput voteBodyInput) throws NodeRulesException {

        User user = userService.getUserFromSecurityContext();

        voteService.voteNode(user.getStableId(), voteBodyInput.nodeStableId, VoteType.valueOf(voteBodyInput.voteType.toUpperCase()));
    }

    public static class UnvoteBodyInput {
        public String nodeStableId;
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/unvoteBody")
    public void unvoteBody(@RequestBody UnvoteBodyInput unvoteBodyInput) throws NodeRulesException {

        User user = userService.getUserFromSecurityContext();

        voteService.unvoteNode(unvoteBodyInput.nodeStableId, user.getStableId());
    }


}
