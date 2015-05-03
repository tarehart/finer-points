package com.nodestand.controllers;

import com.nodestand.nodes.*;
import com.nodestand.nodes.ArgumentNodeRepository;
import com.nodestand.service.DatabasePopulator;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.*;

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


        try (Transaction tx = graphDatabase.beginTx()) {

            Result<Map<String, Object>> result = graphDatabase.queryEngine().query(
                    "match (argument:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody)-[:AUTHORED_BY]->(author:User) " +
                            "return { id: id(argument), title: body.title, labels: labels(argument), " +
                            "author: {login: author.login, name: author.name}, " +
                            "version: body.majorVersion + '.' + body.minorVersion + '.' + argument.buildVersion} as Obj", null);


            List<Object> nodes = new LinkedList<>();

            for (Map<String, Object> map: result) {


                nodes.add(map.get("Obj"));
            }


            model.addAttribute("nodes", nodes);

            tx.success();

            return "list";
        }

    }

    @RequestMapping(value = "/generateTestData", method = RequestMethod.POST)
    public String createTestData(Model model) {

        populator.populateDatabase();

        return "redirect:list";
    }
}
