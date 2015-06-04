package com.nodestand.controllers;

import com.nodestand.dao.GraphDao;
import com.nodestand.nodes.ArgumentNodeRepository;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class GraphController {

    @Autowired
    ArgumentNodeRepository repo;

    @Autowired
    GraphDao graphDao;

    @Transactional
    @PreAuthorize(value = "permitAll")
    @RequestMapping("/graph")
    public Map<String, Object> getGraph(@RequestParam(value="rootId", required=true) long rootId) {
        return graphDao.getGraph(rootId);
    }

}
