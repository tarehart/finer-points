package com.nodestand.controllers;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.comment.Commentable;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.repository.CommentableRepository;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.neo4j.ogm.session.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class DetailController {

    @Autowired
    ArgumentNodeRepository repo;

    @Autowired
    Neo4jOperations neo4jOperations;

    @Autowired
    ArgumentNodeRepository argumentNodeRepository;

    @Autowired
    CommentableRepository commentableRepository;

    @Transactional
    @PreAuthorize("permitAll")
    @RequestMapping("/detail")
    public Object getGraph(@RequestParam(value="id", required=true) String id) {

        // id represents an ArgumentNode id.

        ArgumentNode baseNode = argumentNodeRepository.findOne(Long.parseLong(id));

        Map<String, Object> params = new HashMap<>();
        params.put("id", baseNode.getBody().getId());

        Result result = neo4jOperations.query("start n=node({id}) " +
                "match n-[:VERSION_OF]->(mv:MajorVersion) " +
                "with mv " +
                "match mv<-[:VERSION_OF]-(argBody:ArgumentBody)<-[resp:RESPONDS_TO*0..]-(node:Commentable)-[:AUTHORED_BY]->author " +
                "return resp", params);

        Set<Commentable> comments = commentableRepository.getComments(baseNode.getBody().getId());

        List<Commentable> nodes = new LinkedList<>();
        Set<List<Long>> edges = new HashSet<>();
        Map<String, Object> everything = new HashMap<>();

        for (Map<String, Object> map: result) {
            List<RelationshipProxy> rels = (List<RelationshipProxy>) map.get("resp");
            for (RelationshipProxy rel: rels) {
                edges.add(Arrays.asList(
                        rel.getStartNode().getId(),
                        rel.getEndNode().getId()));
            }
        }

        everything.put("nodes", comments);
        everything.put("edges", edges);
        everything.put("node", baseNode);

        return everything;

    }

}
