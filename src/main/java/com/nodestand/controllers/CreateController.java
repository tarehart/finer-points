package com.nodestand.controllers;

import com.nodestand.dao.GraphDao;
import com.nodestand.nodes.ArgumentNodeRepository;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.version.VersionHelper;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class CreateController {

    @Autowired
    ArgumentNodeRepository repo;

    @Autowired
    GraphDao graphDao;

    @Autowired
    NodeUserDetailsService nodeUserDetailsService;

    @Autowired
    VersionHelper versionHelper;

    @Autowired
    ArgumentNodeRepository nodeRepository;

    @Transactional
    @PreAuthorize("hasRole('ROLE_USER')")
    @RequestMapping("/create")
    public Map<String, Object> createAssertion(
            @RequestParam(value="title") String title,
            @RequestParam(value="body") String body,
            @RequestParam(value="parentId", required=false) Long parentId) {

        User user = nodeUserDetailsService.getUserFromSession();

        AssertionBody assertionBody = new AssertionBody(title, body, user);

        AssertionNode node = assertionBody.constructNode(versionHelper);

        // TODO: create draft nodes based on body text

        nodeRepository.save(node);

        return graphDao.getGraph(node.getId());

    }

}
