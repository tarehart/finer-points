package com.nodestand.dao;

import com.nodestand.controllers.serial.QuickEdge;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.neo4j.ogm.session.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GraphDao {

    @Autowired
    Neo4jOperations neo4jOperations;

    @Autowired
    Neo4jTemplate neo4jTemplate;

    public QuickGraphResponse getGraph(long rootId) {

        Map<String, Object> params = new HashMap<>();
        params.put("id", rootId);

        Result result = neo4jTemplate.query("start n=node({id}) " +
                "match n-[support:SUPPORTED_BY|INTERPRETS*0..5]->(argument:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody)-[:AUTHORED_BY]->(author:User) " +
                "return {" +
                "id: id(argument), " +
                "type: argument.type, " +
                "body: { id: id(body), title: body.title, " +
                    "author: {id: id(author), displayName: author.displayName}" +
                "}" +
                "} as ArgumentNode, support", params);

        Set<Map<String, Object>> nodes = new HashSet<>();
        Set<QuickEdge> properEdges = new HashSet<>();


        for (Map<String, Object> map: result) {
            nodes.add((Map<String, Object>) map.get("ArgumentNode"));
            List<RelationshipProxy> rels = (List<RelationshipProxy>) map.get("support");
            for (RelationshipProxy rel: rels) {
                properEdges.add(new QuickEdge(rel.getStartNode().getId(), rel.getEndNode().getId()));
            }
        }

        return new QuickGraphResponse(nodes, properEdges, rootId);
    }

    public Map<String, Object> getBodyChoices(long bodyId) {
        Map<String, Object> params = new HashMap<>();
        params.put("id", bodyId);

        Result result = neo4jTemplate.query("start n=node({id}) " +
                "match n-[:VERSION_OF]->(mv:MajorVersion) with mv match mv<-[:VERSION_OF]-(:ArgumentBody)" +
                "<-[:DEFINED_BY]-(node:ArgumentNode) " +
                "return node", params);

        Map<String, Object> everything = new HashMap<>();
        Set<ArgumentNode> nodes = new HashSet<>();

        for (Map<String, Object> map: result) {
            nodes.add(neo4jOperations.convert(map.get("node"), ArgumentNode.class));
        }

        everything.put("nodes", nodes);

        return everything;

    }
}
