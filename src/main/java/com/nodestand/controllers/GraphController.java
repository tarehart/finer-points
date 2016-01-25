package com.nodestand.controllers;

import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.dao.GraphDao;
import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class GraphController {

    @Autowired
    ArgumentNodeRepository repo;

    @Autowired
    GraphDao graphDao;

    @Autowired
    Session session;

    @Transactional
    @RequestMapping("/graph")
    public QuickGraphResponse getGraph(@RequestParam(value="rootStableId", required=true) String rootStableId) {
        return graphDao.getGraph(rootStableId);
    }

    @Transactional
    @RequestMapping("/fullDetail")
    public ArgumentNode getFullDetail(@RequestParam(value="nodeId", required=true) Integer nodeId) {
        ArgumentNode node = session.load(ArgumentNode.class, (long) nodeId);
        session.load(ArgumentBody.class, node.getBody().getId(), 2);
        return node;
    }

}
