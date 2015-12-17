package com.nodestand.dao;

import com.nodestand.controllers.serial.QuickEdge;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class GraphDao {

    @Autowired
    ArgumentNodeRepository argumentNodeRepository;

    @Autowired
    Session session;

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
