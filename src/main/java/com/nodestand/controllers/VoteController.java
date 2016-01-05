package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Set;

@RestController
public class VoteController {

    @Autowired
    ArgumentNodeRepository argumentNodeRepository;

    @Autowired
    Session session;

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/upvoteComment")
    public void upvoteComment(@RequestBody Map<String, Object> params) throws NodeRulesException {

        User user = nodeUserDetailsService.getUserFromSession();

        String commentId = (String) params.get("commentId");

        Comment comment = session.load(Comment.class, Long.parseLong(commentId));

        if (comment.author.getNodeId().equals(user.getNodeId())) {
            throw new NodeRulesException("Can't upvote your own comment!");
        }

        comment.registerUpVote(user);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/voteBody")
    public void voteBody(@RequestBody Map<String, Object> params) throws NodeRulesException {
        User user = nodeUserDetailsService.getUserFromSession();

        Long bodyId = Long.valueOf((Integer) params.get("bodyId"));
        ArgumentBody body = session.load(ArgumentBody.class, bodyId);
        String voteType = (String) params.get("voteType");

        switch (voteType) {
            case "great":
                body.registerGreatVote(user);
                break;
            case "weak":
                body.registerWeakVote(user);
                break;
            case "touche":
                body.registerToucheVote(user);
                break;
            case "trash":
                body.registerTrashVote(user);
                break;
            default:
                throw new NodeRulesException("Unrecognized vote type: " + voteType);
        }
        session.save(body);
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/unvoteBody")
    public void unvoteBody(@RequestBody Map<String, Object> params) throws NodeRulesException {
        User user = nodeUserDetailsService.getUserFromSession();

        Long bodyId = Long.valueOf((Integer) params.get("bodyId"));
        ArgumentBody body = session.load(ArgumentBody.class, bodyId);
        body.revokeVote(user);
        session.save(body);
    }


}
