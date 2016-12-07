package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.service.argument.ArgumentService;
import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
public class CreateController {

    private final ArgumentService argumentService;

    private final UserService userService;

    @Autowired
    public CreateController(ArgumentService argumentService, UserService userService) {
        this.argumentService = argumentService;
        this.userService = userService;
    }

    /**
     * For now, this will always mark the newly created node as a draft. There will be a separate operation
     * called 'publish' which will impose more rules.
     *
     * Bodies currently link to major version id, text wise. Nodes link to minor versions.
     * - We do not want to create multiple minor versions as people make draft edits; that should only happen after
     * publishing.
     * - Can we just say that draft-mode edits don't do anything at all to the version number?
     *
     *
     * @param params
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createAssertion")
    public ArgumentNode createAssertion(@RequestBody Map<String, Object> params) throws NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();
        String title = (String) params.get("title");
        String qualifier = (String) params.get("qualifier");
        String body = (String) params.get("body");
        String authorStableId = (String) params.get("authorStableId");
        List<Integer> linkedNodes = (List<Integer>) params.get("links");

        return argumentService.createAssertion(userId, authorStableId, title, qualifier, body, convertToLong(linkedNodes));
    }

    private List<Long> convertToLong(List<Integer> links) {
        return links.stream().map(Long::new).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createInterpretation")
    public ArgumentNode createInterpretation(@RequestBody Map<String, Object> params) throws NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();
        String title = (String) params.get("title");
        String qualifier = (String) params.get("qualifier");
        String body = (String) params.get("body");
        String authorStableId = (String) params.get("authorStableId");

        Long sourceId = null;
        if (params.get("sourceId") != null) {
            sourceId = Long.valueOf((Integer) params.get("sourceId"));
        }

        return argumentService.createInterpretation(userId, authorStableId, title, qualifier, body, sourceId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createSource")
    public ArgumentNode createSource(@RequestBody Map<String, Object> params) throws NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();
        String title = (String) params.get("title");
        String qualifier = (String) params.get("qualifier");
        String url = (String) params.get("url");
        String authorStableId = (String) params.get("authorStableId");

        return argumentService.createSource(userId, authorStableId, title, qualifier, url);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/nodesInMajorVersion")
    public Set<ArgumentNode> getNodesInMajorVersion(@RequestParam Long majorVersionId) {

        return argumentService.getNodesInMajorVersion(majorVersionId);
    }
}
