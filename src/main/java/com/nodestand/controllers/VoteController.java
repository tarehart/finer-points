package com.nodestand.controllers;

import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.version.MajorVersion;
import com.nodestand.nodes.vote.VoteType;
import com.nodestand.service.user.UserService;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class VoteController {

    private final Session session;

    private final UserService userService;

    @Autowired
    public VoteController(Session session, UserService userService) {
        this.session = session;
        this.userService = userService;
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

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/voteBody")
    public void voteBody(@RequestBody Map<String, Object> params) throws NodeRulesException {
        Long userId = userService.getUserNodeIdFromSecurityContext();
        User user = session.load(User.class, userId);

        Long majorVersionId = Long.valueOf((Integer) params.get("majorVersionId"));
        MajorVersion mv = session.load(MajorVersion.class, majorVersionId);
        String voteTypeStr = (String) params.get("voteType");
        VoteType voteType = VoteType.valueOf(voteTypeStr.toUpperCase());

        user.registerVote(mv, voteType);

        session.save(mv);
        session.save(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/unvoteBody")
    public void unvoteBody(@RequestBody Map<String, Object> params) throws NodeRulesException {
        Long userId = userService.getUserNodeIdFromSecurityContext();
        User user = session.load(User.class, userId);

        Long majorVersionId = Long.valueOf((Integer) params.get("majorVersionId"));
        MajorVersion mv = session.load(MajorVersion.class, majorVersionId);
        user.revokeVote(mv);

        session.save(mv);
        session.save(user);
    }


}
