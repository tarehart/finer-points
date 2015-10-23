package com.nodestand.dao;

import com.nodestand.controllers.serial.QuickEdge;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.neo4j.helpers.collection.IteratorUtil;
import org.neo4j.kernel.impl.core.RelationshipProxy;
import org.neo4j.ogm.cypher.query.GraphModelQuery;
import org.neo4j.ogm.cypher.query.Query;
import org.neo4j.ogm.model.GraphModel;
import org.neo4j.ogm.session.Neo4jSession;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.session.response.Neo4jResponse;
import org.neo4j.ogm.session.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GraphDao {

    @Autowired
    Neo4jOperations neo4jOperations;

    @Autowired
    ArgumentNodeRepository argumentNodeRepository;

    @Autowired
    Neo4jSession session;

    public QuickGraphResponse getGraph(long rootId) {

        Map<String, Object> params = new HashMap<>();
        params.put("id", rootId);

//        Result result = neo4jOperations.query("start n=node({id}) " +
//                "match n-[support:SUPPORTED_BY|INTERPRETS*0..5]->(argument:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody)-[:AUTHORED_BY]->(author:User) " +
//                "return {" +
//                "id: id(argument), " +
//                "type: argument.type, " +
//                "body: { id: id(body), title: body.title, " +
//                    "author: {id: id(author), displayName: author.displayName}" +
//                "}" +
//                "} as ArgumentNode, {start: id(startNode(support)), end: id(endNode(support))} as theSupport", params);

        Set<ArgumentNode> nodes = argumentNodeRepository.getGraph(rootId);

        String cypher = "start n=node({id}) match path=n-[support:SUPPORTED_BY|INTERPRETS*0..5]->(argument:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody) return path";

        Iterable<ArgumentNode> otherNodes = neo4jOperations.queryForObjects(ArgumentNode.class, cypher, params);
        Set<ArgumentNode> nodeSet = new HashSet<>();
        IteratorUtil.addToCollection(otherNodes, nodeSet);

        Query qry = new GraphModelQuery(cypher, params);
        Neo4jResponse<GraphModel> graphModel = session.requestHandler().execute(qry, session.ensureTransaction().url());

//        Set<Map<String, Object>> nodes = new HashSet<>();
//        Set<QuickEdge> properEdges = new HashSet<>();
//
//
//        for (Map<String, Object> map: result) {
//            nodes.add((Map<String, Object>) map.get("ArgumentNode"));
//            List<RelationshipProxy> rels = (List<RelationshipProxy>) map.get("support");
//            for (RelationshipProxy rel: rels) {
//                properEdges.add(new QuickEdge(rel.getStartNode().getId(), rel.getEndNode().getId()));
//            }
//        }

        return null;

        //return new QuickGraphResponse(nodes, properEdges, rootId);
    }

    public Map<String, Object> getBodyChoices(long bodyId) {

        Map<String, Object> everything = new HashMap<>();
        Set<ArgumentNode> nodes = argumentNodeRepository.getBodyChoices(bodyId);

        everything.put("nodes", nodes);

        return everything;

    }
}
