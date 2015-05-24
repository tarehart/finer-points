package com.nodestand.controllers;

import com.nodestand.dao.NodeListDao;
import com.nodestand.nodes.ArgumentNodeRepository;
import com.nodestand.service.DatabasePopulator;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Controller
public class ListController {
    @Autowired
    ArgumentNodeRepository repo;

    @Autowired
    GraphDatabase graphDatabase;

    @Autowired
    DatabasePopulator populator;

    @RequestMapping("/list")
    public String getGraph(Model model) {

            List<Object> nodes = NodeListDao.getAllNodes(graphDatabase);

            model.addAttribute("nodes", nodes);

            return "list";


    }

    @RequestMapping(value = "/generateTestData", method = RequestMethod.POST)
    public String createTestData(Model model) {

        populator.populateDatabase();

        return "redirect:list";
    }
}
