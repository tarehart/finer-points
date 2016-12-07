package com.nodestand.controllers;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.controllers.serial.EditResult;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.service.argument.ArgumentService;
import com.nodestand.service.user.UserService;
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

    private final ArgumentService argumentService;

    private final UserService userService;

    @Autowired
    public EditController(ArgumentService argumentService, UserService userService) {
        this.argumentService = argumentService;
        this.userService = userService;
    }

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
        Long userId = userService.getUserNodeIdFromSecurityContext();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));
        String title = (String) params.get("title");
        String qualifier = (String) params.get("qualifier");
        String body = (String) params.get("body");
        List<Integer> links = (List<Integer>) params.get("links");

        return argumentService.editAssertion(userId, nodeId, title, qualifier, body, convertToLong(links));
    }

    private List<Long> convertToLong(List<Integer> links) {
        return links.stream().map(Long::new).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editInterpretation")
    public ArgumentNode editInterpretation(@RequestBody Map<String, Object> params) throws NotAuthorizedException, NodeRulesException {

        // TODO: fix these casts, they can throw null pointers.

        Long userId = userService.getUserNodeIdFromSecurityContext();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));
        String title = (String) params.get("title");
        String qualifier = (String) params.get("qualifier");
        String body = (String) params.get("body");
        Long sourceId = (long) (Integer) params.get("sourceId");

        return argumentService.editInterpretation(userId, nodeId, title, qualifier, body, sourceId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editSource")
    public ArgumentNode editSource(@RequestBody Map<String, Object> params) throws NotAuthorizedException, NodeRulesException {
        Long userId = userService.getUserNodeIdFromSecurityContext();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));
        String title = (String) params.get("title");
        String qualifier = (String) params.get("qualifier");
        String url = (String) params.get("url");

        return argumentService.editSource(userId, nodeId, title, qualifier, url);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/makeDraft")
    public EditResult makeDraft(@RequestBody Map<String, Object> params) throws NotAuthorizedException, NodeRulesException {

        // TODO: make ajax pass authorStableId

        Long userId = userService.getUserNodeIdFromSecurityContext();
        Long nodeId = Long.valueOf((Integer) params.get("nodeId"));
        String authorStableId = (String) params.get("authorStableId");

        return argumentService.makeDraft(userId, authorStableId, nodeId);
    }
}
