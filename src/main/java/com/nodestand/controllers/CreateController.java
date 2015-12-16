package com.nodestand.controllers;

import com.nodestand.dao.GraphDao;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.interpretation.InterpretationBody;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentBodyRepository;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.source.SourceBody;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.version.VersionHelper;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.ogm.session.Neo4jSession;
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

    @Autowired
    ArgumentBodyRepository bodyRepository;

    @Autowired
    Neo4jSession session;

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
    @RequestMapping("/createAssertion")
    public ArgumentNode createAssertion(@RequestBody Map<String, Object> params) {

        User user = nodeUserDetailsService.getUserFromSession();

        AssertionBody assertionBody = new AssertionBody((String) params.get("title"), (String) params.get("body"), user);
        List<Integer> linkedNodes = (List<Integer>) params.get("links");

        AssertionNode node = assertionBody.constructNode(versionHelper);

        for (Integer id : linkedNodes) {
            ArgumentNode linked = nodeRepository.findOne(Long.valueOf(id));
            node.supportedBy(linked);
        }

        session.save(node);
        return node;
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createInterpretation")
    public ArgumentNode createInterpretation(@RequestBody Map<String, Object> params) {

        User user = nodeUserDetailsService.getUserFromSession();

        InterpretationBody interpretationBody = new InterpretationBody((String) params.get("title"), (String) params.get("body"), user);

        InterpretationNode node = interpretationBody.constructNode(versionHelper);

        if (params.get("sourceId") != null) {
            Long sourceId = Long.valueOf((Integer) params.get("sourceId"));
            SourceNode source = (SourceNode) nodeRepository.findOne(sourceId);
            node.setSource(source);
        }

        session.save(node);
        return node;
    }


    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/createSource")
    public ArgumentNode createSource(@RequestBody Map<String, Object> params) {

        User user = nodeUserDetailsService.getUserFromSession();

        SourceBody sourceBody = new SourceBody((String) params.get("title"), user, (String) params.get("url"));

        SourceNode node = sourceBody.constructNode(versionHelper);

        session.save(node);
        return node;
    }

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/bodyChoices")
    public Map<String, Object> getBodyChoices(@RequestParam Long bodyId) {

        return graphDao.getBodyChoices(bodyId);

    }
}
