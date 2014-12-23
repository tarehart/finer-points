package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentNodeRepository;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.conversion.Result;
import org.springframework.data.neo4j.core.GraphDatabase;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class GraphController {

    @Autowired
    ArgumentNodeRepository repo;

    @Autowired
    GraphDatabase graphDatabase;

    @Transactional
    @RequestMapping("/graph")
    public Map<String, Object> getGraph(@RequestParam(value="rootId", required=true) String rootId, Model model) {

            Map<String, Object> params = new HashMap<>();
            params.put( "id", Integer.parseInt(rootId) );

            Result<Map<String, Object>> result = graphDatabase.queryEngine().query("start n=node({id}) " +
                    "match n-[support:SUPPORTED_BY|INTERPRETS*0..5]->argument-[:AUTHORED_BY*1..]->author " +
                    "return {" +
                        "id: id(argument), " +
                        "title: argument.title, " +
                        "author: author.name" +
                    "} as ArgumentNode, support", params);


            List<Map<String, Object>> nodes = new LinkedList<>();
            Set<List<Long>> edges = new HashSet<>();
            Map<String, Object> everything = new HashMap<>();

            for (Map<String, Object> map: result) {
                nodes.add((Map<String, Object>) map.get("ArgumentNode"));
                List<RelationshipProxy> rels = (List<RelationshipProxy>) map.get("support");
                for (RelationshipProxy rel: rels) {
                    edges.add(Arrays.asList(
                            rel.getStartNode().getId(),
                            rel.getEndNode().getId()));
                }
            }

            everything.put("nodes", nodes);
            everything.put("edges", edges);



            return everything;

    }

}
