package com.nodestand.controllers;

import com.nodestand.nodes.*;
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

@Controller
public class ListController {
    @Autowired
    ArgumentNodeRepository repo;

    @Autowired
    GraphDatabase graphDatabase;

    @RequestMapping("/list")
    public String getGraph(Model model) {


        try (Transaction tx = graphDatabase.beginTx()) {
            Result<ArgumentNode> result = repo.findAll();

            List<ArgumentNode> list = new LinkedList<>();
            for (ArgumentNode an: result) {
                list.add(an);
            }

            model.addAttribute("nodes", list);

            tx.success();

            return "list";
        }

    }

    @RequestMapping(value = "/generateTestData", method = RequestMethod.POST)
    public String createTestData(Model model) {

        Assertion tablesHelpful = new Assertion("Tables are helpful for meals.");

        Assertion mealsBenefit = new Assertion("It is easier to eat a meal if you have a flat surface");
        Interpretation tablesInterp = new Interpretation("Tables weekly says that tables provide a flat surface");

        Source tablesWeekly = new Source("Tables Weekly, vol 32", "http://www.google.com");

        try (Transaction tx = graphDatabase.beginTx()) {
            repo.save(tablesHelpful);
            repo.save(mealsBenefit);
            repo.save(tablesInterp);
            repo.save(tablesWeekly);

            tablesHelpful.supportedBy(tablesInterp);
            tablesHelpful.supportedBy(mealsBenefit);

            tablesInterp.setSource(tablesWeekly);

            repo.save(tablesHelpful);
            repo.save(tablesInterp);

            tx.success();
        }

        return "redirect:list";
    }
}
