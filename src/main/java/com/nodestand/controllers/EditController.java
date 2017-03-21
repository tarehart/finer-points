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
    public ArgumentNode editAssertion(@RequestBody EditAssertionInput input) throws NotAuthorizedException, NodeRulesException {
        Long userId = userService.getUserNodeIdFromSecurityContext();

        return argumentService.editAssertion(userId, input.nodeId, input.title, input.qualifier, input.body, input.links);
    }

    public static class EditAssertionInput {
        public EditAssertionInput() {}
        public Long nodeId;
        public String title;
        public String qualifier;
        public String body;
        public List<Long> links;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editInterpretation")
    public ArgumentNode editInterpretation(@RequestBody EditInterpretationInput input) throws NotAuthorizedException, NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();

        return argumentService.editInterpretation(userId, input.nodeId, input.title, input.qualifier, input.body, input.sourceId);
    }

    public static class EditInterpretationInput {
        public EditInterpretationInput() {}
        public Long nodeId;
        public String title;
        public String qualifier;
        public String body;
        public Long sourceId;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editSource")
    public ArgumentNode editSource(@RequestBody EditLeafInput input) throws NotAuthorizedException, NodeRulesException {
        Long userId = userService.getUserNodeIdFromSecurityContext();

        return argumentService.editSource(userId, input.nodeId, input.title, input.qualifier, input.url);
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/editSubject")
    public ArgumentNode editSubject(@RequestBody EditLeafInput input) throws NotAuthorizedException, NodeRulesException {
        Long userId = userService.getUserNodeIdFromSecurityContext();

        return argumentService.editSubject(userId, input.nodeId, input.title, input.qualifier, input.url);
    }

    public static class EditLeafInput {
        public EditLeafInput() {}
        public Long nodeId;
        public String title;
        public String qualifier;
        public String url;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/makeDraft")
    public EditResult makeDraft(@RequestBody MakeDraftInput input) throws NotAuthorizedException, NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();

        return argumentService.makeDraft(userId, input.authorStableId, input.nodeId);
    }

    public static class MakeDraftInput {
        public MakeDraftInput() {}
        public Long nodeId;
        public String authorStableId;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/discardDraft")
    public void makeDraft(@RequestBody String stableId) throws NotAuthorizedException, NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();

        argumentService.discardDraft(userId, stableId);
    }
}
