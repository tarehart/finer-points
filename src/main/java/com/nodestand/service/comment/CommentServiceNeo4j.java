package com.nodestand.service.comment;

import com.nodestand.controllers.serial.QuickCommentResponse;
import com.nodestand.controllers.serial.QuickEdge;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.repository.CommentableRepository;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class CommentServiceNeo4j implements CommentService {

    @Autowired
    Session session;

    @Autowired
    CommentableRepository commentRepo;

    @Override
    @Transactional
    public QuickCommentResponse getComments(long argumentNodeId) {
        ArgumentNode baseNode = session.load(ArgumentNode.class, argumentNodeId, 2);

        Set<Commentable> comments = commentRepo.getComments(baseNode.getBody().getId());

        Set<QuickEdge> edges = new HashSet<>();

        comments.stream().filter(c -> c instanceof Comment).forEach(c -> {
            Comment comment = (Comment) c;
            edges.add(new QuickEdge(comment.getId(), comment.parent.getId()));
        });

        return new QuickCommentResponse(comments, edges, baseNode.getBody().getId());
    }

    @Override
    @Transactional
    public Comment createComment(String body, long parentId, long userId) {
        Commentable parent = session.load(Commentable.class, parentId);

        User user = session.load(User.class, userId);

        Comment comment = new Comment(parent, user, body);

        session.save(comment);

        return comment;
    }

    @Override
    @Transactional
    public Comment editComment(String body, long commentId, long userId) throws NodeRulesException {
        Comment comment = session.load(Comment.class, commentId);

        if (!comment.author.getNodeId().equals(userId)) {
            throw new NodeRulesException("Can't edit somebody else's comment!");
        }

        comment.setDateEdited(new Date());
        comment.body = body;

        session.save(comment);

        return comment;
    }
}