package com.nodestand.controllers;

import com.nodestand.dao.NodeListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class NodeMenuController {

    @Autowired
    Neo4jOperations neo4jOperations;

    @Transactional
    @RequestMapping("/nodeMenu")
    public List<Object> getNodeMenu() {

        return NodeListDao.getAllNodes(neo4jOperations);
    }

}
