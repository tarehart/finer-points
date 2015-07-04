package com.nodestand.controllers;

import com.nodestand.dao.GraphDao;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.version.VersionHelper;
import com.nodestand.service.NodeUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CreateController {

    @Autowired
    GraphDao graphDao;

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Autowired
    VersionHelper versionHelper;

    @Autowired
    ArgumentNodeRepository nodeRepository;

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
    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/create")
    public Map<String, Object> createAssertion(@RequestBody Map<String, Object> params) {

        User user = nodeUserDetailsService.getUserFromSession();

        AssertionBody assertionBody = new AssertionBody((String) params.get("title"), (String) params.get("body"), user);
        List<Integer> linkedNodes = (List<Integer>) params.get("links");

        AssertionNode node = assertionBody.constructNode(versionHelper);

        for (Integer id : linkedNodes) {
            ArgumentNode linked = nodeRepository.findOne(Long.valueOf(id));
            node.supportedBy(linked);
        }

        // If the node is saved without an author, it will not be found by the list query
        // because the author is part of the pattern matching.

        // When inspecting neo4j, be careful that you use MATCH n RETURN n LIMIT <high number>,
        // otherwise the default of 25 will really confuse you.

        nodeRepository.save(node);

        return graphDao.getGraph(node.getId());

    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/bodyChoices")
    public Map<String, Object> getBodyChoices(@RequestParam Long bodyId) {

        return graphDao.getBodyChoices(bodyId);

    }
}
