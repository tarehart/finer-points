package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.service.user.UserService;
import com.nodestand.service.argument.ArgumentService;
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

    @Autowired
    ArgumentService argumentService;

    @Autowired
    UserService userService;

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
    public ArgumentNode createAssertion(@RequestBody Map<String, Object> params) {

        Long userId = userService.getUserIdFromSession();
        String title = (String) params.get("title");
        String body = (String) params.get("body");
        List<Integer> linkedNodes = (List<Integer>) params.get("links");

        return argumentService.createAssertion(userId, title, body, convertToLong(linkedNodes));
    }

    private List<Long> convertToLong(List<Integer> links) {
        return links.stream().map(Long::new).collect(Collectors.toList());
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createInterpretation")
    public ArgumentNode createInterpretation(@RequestBody Map<String, Object> params) {

        Long userId = userService.getUserIdFromSession();
        String title = (String) params.get("title");
        String body = (String) params.get("body");

        Long sourceId = null;
        if (params.get("sourceId") != null) {
            sourceId = Long.valueOf((Integer) params.get("sourceId"));
        }

        return argumentService.createInterpretation(userId, title, body, sourceId);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createSource")
    public ArgumentNode createSource(@RequestBody Map<String, Object> params) {

        Long userId = userService.getUserIdFromSession();
        String title = (String) params.get("title");
        String url = (String) params.get("url");

        return argumentService.createSource(userId, title, url);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/nodesInMajorVersion")
    public Set<ArgumentNode> getNodesInMajorVersion(@RequestParam Long majorVersionId) {

        return argumentService.getNodesInMajorVersion(majorVersionId);
    }
}
