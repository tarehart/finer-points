package com.nodestand.service.comment;

import com.nodestand.controllers.serial.QuickCommentResponse;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.comment.Comment;

public interface CommentService {

    QuickCommentResponse getComments(long majorVersionId);

    Comment createComment(String body, long parentId, long userId);

    Comment editComment(String body, long commentId, long userId) throws NodeRulesException;
}
