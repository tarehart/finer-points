package com.nodestand.controllers;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.controllers.serial.EditResult;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.service.user.UserService;
import com.nodestand.service.argument.ArgumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class EditController {

    @Autowired
    ArgumentService argumentService;

    @Autowired
    UserService userService;

    /**
     * Hyperlinks within body text have an id corresponding to a major version. That way we don't have to update them
     * when children are directly edited and thereby have their minor versions changed. The real links are managed by
     * the graph database, where nodes (not bodies) link to other nodes (which are at the build version level). Giving
     * they hyperlinks the node id of the major version will be sufficient to map to the correct child node.
     *
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editAssertion")
    public ArgumentNode editAssertion(@RequestBody Map<String, Object> params) throws NotAuthorizedException, NodeRulesException {
        Long userId = userService.getUserIdFromSession();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));
        String title = (String) params.get("title");
        String body = (String) params.get("body");
        List<Integer> links = (List<Integer>) params.get("links");

        return argumentService.editAssertion(userId, nodeId, title, body, convertToLong(links));
    }

    private List<Long> convertToLong(List<Integer> links) {
        return links.stream().map(Long::new).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editInterpretation")
    public ArgumentNode editInterpretation(@RequestBody Map<String, Object> params) throws NotAuthorizedException, NodeRulesException {
        Long userId = userService.getUserIdFromSession();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));
        String title = (String) params.get("title");
        String body = (String) params.get("body");
        Long sourceId = (long) (Integer) params.get("sourceId");

        return argumentService.editInterpretation(userId, nodeId, title, body, sourceId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editSource")
    public ArgumentNode editSource(@RequestBody Map<String, Object> params) throws NotAuthorizedException, NodeRulesException {
        Long userId = userService.getUserIdFromSession();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));
        String title = (String) params.get("title");
        String url = (String) params.get("url");

        return argumentService.editSource(userId, nodeId, title, url);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/makeDraft")
    public EditResult makeDraft(@RequestBody Map<String, Object> params) throws NotAuthorizedException, NodeRulesException {
        Long userId = userService.getUserIdFromSession();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));
        String rootStableId = (String) params.get("rootStableId");

        return argumentService.makeDraft(userId, nodeId, rootStableId);
    }
}
