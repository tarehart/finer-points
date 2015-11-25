package com.nodestand.dao;

import com.nodestand.controllers.serial.QuickEdge;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.neo4j.ogm.session.Neo4jSession;
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

    public QuickGraphResponse getGraph(String rootStableId) {

        Set<ArgumentNode> nodes = argumentNodeRepository.getGraph(rootStableId);

        Set<QuickEdge> edges = new HashSet<>();

        Long rootId = null;

        for (ArgumentNode n: nodes) {
            for (ArgumentNode child : n.getGraphChildren()) {
                edges.add(new QuickEdge(n.getId(), child.getId()));
            }
            if (n.getStableId().equals(rootStableId)) {
                rootId = n.getId();
            }
        }

        return new QuickGraphResponse(nodes, edges, rootId, rootStableId);

    }

    public Map<String, Object> getBodyChoices(long bodyId) {

        Map<String, Object> everything = new HashMap<>();
        Set<ArgumentNode> nodes = argumentNodeRepository.getBodyChoices(bodyId);

        everything.put("nodes", nodes);

        return everything;

    }
}
