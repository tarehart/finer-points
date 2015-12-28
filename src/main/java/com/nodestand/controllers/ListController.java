package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.service.DatabasePopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Set;

@Controller
public class ListController {
    @Autowired
    ArgumentNodeRepository repo;

    @Autowired
    DatabasePopulator populator;

    @RequestMapping("/list")
    public String getGraph(Model model) {

        Set<ArgumentNode> otherNodes = repo.getAllNodes();

        model.addAttribute("nodes", otherNodes);

        return "list";
    }



    @RequestMapping(value = "/generateTestData", method = RequestMethod.POST)
    public String createTestData(Model model) {

        populator.populateDatabase();

        return "redirect:list";
    }
}
