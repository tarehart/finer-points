package com.nodestand.service.comment;

import com.nodestand.controllers.serial.QuickCommentResponse;
import com.nodestand.controllers.serial.QuickEdge;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.repository.CommentableRepository;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.service.AuthorRulesUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

@Component
public class CommentServiceNeo4j implements CommentService {

    private final Neo4jOperations operations;

    private final CommentableRepository commentRepo;

    private final ArgumentNodeRepository argumentRepo;

    private final UserRepository userRepo;

    @Autowired
    public CommentServiceNeo4j(Neo4jOperations operations, CommentableRepository commentRepo, ArgumentNodeRepository argumentRepo, UserRepository userRepo) {
        this.operations = operations;
        this.commentRepo = commentRepo;
        this.argumentRepo = argumentRepo;
        this.userRepo = userRepo;
    }

    @Override
    @Transactional
    public QuickCommentResponse getComments(long majorVersionId) {

        Set<Commentable> comments = commentRepo.getComments(majorVersionId);

        Set<QuickEdge> edges = new HashSet<>();

        comments.stream().filter(c -> c instanceof Comment).forEach(c -> {
            Comment comment = (Comment) c;
            edges.add(new QuickEdge(comment.getId(), comment.parent.getId()));
        });

        return new QuickCommentResponse(comments, edges);
    }

    @Override
    @Transactional
    public Comment createComment(String body, long parentId, String authorStableId, long userId) throws NodeRulesException {

        Author author = AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, authorStableId);
        Commentable parent = operations.load(Commentable.class, parentId);

        Comment comment = new Comment(parent, author, body);

        operations.save(comment);

        return comment;
    }

    @Override
    @Transactional
    public Comment editComment(String body, long commentId, String userStableId) throws NodeRulesException {
        Comment comment = operations.load(Comment.class, commentId);

        User user = userRepo.getUser(userStableId);

        if (user.getAliases().stream().noneMatch(a -> a.getStableId().equals(comment.author.getStableId()))) {
            throw new NodeRulesException("Can't edit somebody else's comment!");
        }

        comment.setDateEdited(new Date());
        comment.body = body;

        operations.save(comment);

        return comment;
    }
}
