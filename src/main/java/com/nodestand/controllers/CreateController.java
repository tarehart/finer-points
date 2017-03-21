package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.assertion.AssertionBody;
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
     * @return
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createAssertion")
    public ArgumentNode createAssertion(@RequestBody CreateAssertionInput input) throws NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();

        return argumentService.createAssertion(userId, input.authorStableId, input.title, input.qualifier, input.body, input.links);
    }

    private List<Long> convertToLong(List<Integer> links) {
        return links.stream().map(Long::new).collect(Collectors.toList());
    }

    public static class CreateAssertionInput {
        public CreateAssertionInput() {}
        public String title;
        public String qualifier;
        public String body;
        public String authorStableId;
        public List<Long> links;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createInterpretation")
    public ArgumentNode createInterpretation(@RequestBody CreateInterpretationInput input) throws NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();

        return argumentService.createInterpretation(userId, input.authorStableId, input.title, input.qualifier,
                input.body, input.leafId);
    }

    public static class CreateInterpretationInput {
        public CreateInterpretationInput() {}
        public String title;
        public String qualifier;
        public String body;
        public String authorStableId;
        public Long leafId;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createSource")
    public ArgumentNode createSource(@RequestBody CreateLeafInput input) throws NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();

        return argumentService.createSource(userId, input.authorStableId, input.title, input.qualifier, input.url);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createSubject")
    public ArgumentNode createSubject(@RequestBody CreateLeafInput input) throws NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();

        return argumentService.createSubject(userId, input.authorStableId, input.title, input.qualifier, input.url);
    }

    public static class CreateLeafInput {
        public CreateLeafInput() {}
        public String title;
        public String qualifier;
        public String url;
        public String authorStableId;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/nodesInMajorVersion")
    public Set<ArgumentNode> getNodesInMajorVersion(@RequestParam Long majorVersionId) {

        return argumentService.getNodesInMajorVersion(majorVersionId);
    }
}
