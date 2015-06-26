package com.nodestand.controllers;

import com.nodestand.dao.NodeListDao;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class NodeMenuController {

    @Autowired
    ArgumentNodeRepository repo;

    @Autowired
    GraphDatabase graphDatabase;

    @Transactional
    @RequestMapping("/nodeMenu")
    public List<Object> getNodeMenu() {

            List<Object> list = NodeListDao.getAllNodes(graphDatabase);

            return list;
    }

}
