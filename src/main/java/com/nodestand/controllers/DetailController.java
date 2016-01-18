package com.nodestand.controllers;

import com.nodestand.controllers.serial.QuickCommentResponse;
import com.nodestand.controllers.serial.QuickEdge;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.repository.CommentableRepository;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.Set;

@RestController
public class DetailController {

    @Autowired
    Session session;

    @Autowired
    CommentableRepository commentableRepository;

    @Transactional
    @PreAuthorize("permitAll")
    @RequestMapping("/detail")
    public Object getGraph(@RequestParam(value="id", required=true) String id) {

        // id represents an ArgumentNode id.
        ArgumentNode baseNode = session.load(ArgumentNode.class, Long.parseLong(id), 2);

        Set<Commentable> comments = commentableRepository.getComments(baseNode.getBody().getId());

        Set<QuickEdge> edges = new HashSet<>();

        for (Commentable c : comments) {
            if (c instanceof Comment) {
                Comment comment = (Comment) c;
                edges.add(new QuickEdge(comment.getId(), comment.parent.getId()));
            }
        }

        return new QuickCommentResponse(comments, edges, baseNode.getBody().getId());

    }

}
