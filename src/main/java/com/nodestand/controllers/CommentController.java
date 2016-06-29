package com.nodestand.controllers;

import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.comment.Comment;
import com.nodestand.service.user.UserService;
import com.nodestand.service.comment.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CommentController {

    @Autowired
    CommentService commentService;

    @Autowired
    UserService userService;

    @PreAuthorize("permitAll")
    @RequestMapping("/comments")
    public Object getCommentsOnMajorVersion(@RequestParam(value="id", required=true) Integer id) {

        return commentService.getComments(id);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createComment")
    public Comment createNewComment(@RequestBody Map<String, Object> params) {

        String body = (String) params.get("body");
        Long parentId = Long.valueOf((Integer) params.get("parentId"));
        Long userId = userService.getUserIdFromSession();

        return commentService.createComment(body, parentId, userId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editComment")
    public Comment editComment(@RequestBody Map<String, Object> params) throws NodeRulesException {

        String body = (String) params.get("body");
        Long commentId = Long.valueOf((Integer) params.get("commentId"));
        Long userId = userService.getUserIdFromSession();

        return commentService.editComment(body, commentId, userId);
    }

}
