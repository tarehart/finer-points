package com.nodestand.service.comment;

import com.nodestand.controllers.serial.QuickCommentResponse;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.comment.Commentable;

public interface CommentService {

    QuickCommentResponse getComments(String nodeStableId);

    Comment createComment(String body, long parentId, String authorStableId, long userId) throws NodeRulesException;

    Comment editComment(String body, long commentId, String userStableId) throws NodeRulesException;

    Comment loadWithWatchers(long commentId);
}
