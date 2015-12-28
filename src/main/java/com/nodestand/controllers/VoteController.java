package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
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


}
