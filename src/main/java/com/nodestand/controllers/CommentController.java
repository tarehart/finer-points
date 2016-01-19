package com.nodestand.controllers;

import com.nodestand.controllers.serial.QuickCommentResponse;
import com.nodestand.controllers.serial.QuickEdge;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.User;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.repository.CommentableRepository;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@RestController
public class CommentController {

    @Autowired
    Session session;

    @Autowired
    CommentableRepository commentableRepository;

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Transactional
    @PreAuthorize("permitAll")
    @RequestMapping("/comments")
    public Object getGraph(@RequestParam(value="id", required=true) String id) {

        // id represents an ArgumentNode id.
        ArgumentNode baseNode = session.load(ArgumentNode.class, Long.parseLong(id), 2);

        Set<Commentable> comments = commentableRepository.getComments(baseNode.getBody().getId());

        Set<QuickEdge> edges = new HashSet<>();

        comments.stream().filter(c -> c instanceof Comment).forEach(c -> {
            Comment comment = (Comment) c;
            edges.add(new QuickEdge(comment.getId(), comment.parent.getId()));
        });

        return new QuickCommentResponse(comments, edges, baseNode.getBody().getId());

    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/saveComment")
    public Comment saveComment(@RequestBody Map<String, Object> params) {

        String body = (String) params.get("body");
        Long parentId = Long.valueOf((Integer) params.get("parentId"));
        Commentable parent = session.load(Commentable.class, parentId);

        Long userId = nodeUserDetailsService.getUserIdFromSession();
        User user = session.load(User.class, userId);

        Comment comment = new Comment(parent, user, body);

        session.save(comment);

        return comment;
    }

}
