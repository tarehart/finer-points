package com.nodestand.service;

import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.IndexManager;
import org.springframework.data.neo4j.support.DelegatingGraphDatabase;
import org.springframework.data.neo4j.template.Neo4jOperations;

/**
 * @author mh
 * @since 02.03.11
 */
public class Neo4jDatabaseCleaner {
    private GraphDatabaseService graph;

    public Neo4jDatabaseCleaner(Neo4jOperations template) {
        this.graph = ((DelegatingGraphDatabase)template.getGraphDatabase()).getGraphDatabaseService();
    }

    public void cleanDb() {
        Transaction tx = graph.beginTx();
        try {
            removeNodes();
            clearIndex();
            tx.success();
        } finally {
            tx.close();
        }
    }

    private void removeNodes() {

        ExecutionEngine engine = new ExecutionEngine( graph );

        try ( Transaction ignored = graph.beginTx() ) {
            engine.execute(
                "USING PERIODIC COMMIT " +
                "MATCH (a) " +
                "OPTIONAL MATCH (a)-[r]-() " +
                "DELETE a,r;");

        }

    }

    private void clearIndex() {
        IndexManager indexManager = graph.index();
        //result.put("node-indexes", Arrays.asList(indexManager.nodeIndexNames()));
        //result.put("relationship-indexes", Arrays.asList(indexManager.relationshipIndexNames()));
        for (String ix : indexManager.nodeIndexNames()) {
            indexManager.forNodes(ix).delete();
        }
        for (String ix : indexManager.relationshipIndexNames()) {
            indexManager.forRelationships(ix).delete();
        }
    }
}
