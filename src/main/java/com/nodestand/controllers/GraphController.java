package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.ArgumentNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GraphController {

    @Autowired
    ArgumentNodeRepository repo;

    @RequestMapping("/graph")
    public String getGraph(@RequestParam(value="rootId", required=true) String rootId, Model model) {

        ArgumentNode a = repo.findOne(Long.parseLong(rootId));

        model.addAttribute("assertion", a);

        return "graph";
    }

}
