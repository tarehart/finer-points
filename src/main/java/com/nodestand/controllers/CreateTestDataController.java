package com.nodestand.controllers;

import com.nodestand.nodes.Assertion;
import com.nodestand.nodes.AssertionRepository;
import org.neo4j.graphdb.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CreateTestDataController {

    @Autowired
    AssertionRepository repo;

    @Autowired
    GraphDatabase graphDatabase;

    @RequestMapping("/testdata")
    public String createTestData() {

        Assertion tablesHelpful = new Assertion("Tables are helpful for meals.");
        Assertion mealsBenefit = new Assertion("It is easier to eat a meal if you have a flat surface");
        Assertion tablesProvide = new Assertion("Tables provide a flat surface");



        try (Transaction tx = graphDatabase.beginTx()) {
            repo.save(tablesHelpful);
            repo.save(mealsBenefit);
            repo.save(tablesProvide);

            tablesHelpful.supportedBy(tablesProvide);
            tablesHelpful.supportedBy(mealsBenefit);

            repo.save(tablesHelpful);

            tx.success();
        }

        return "success";
    }

}
