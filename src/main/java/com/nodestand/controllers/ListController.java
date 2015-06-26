package com.nodestand.controllers;

import com.nodestand.dao.NodeListDao;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.service.DatabasePopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.List;

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
