package com.nodestand.controllers;

import com.nodestand.dao.NodeListDao;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.ArgumentNodeRepository;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.NodeList;

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