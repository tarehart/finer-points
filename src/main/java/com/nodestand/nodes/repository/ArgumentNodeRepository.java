package com.nodestand.nodes.repository;

import com.nodestand.nodes.ArgumentNode;
import org.neo4j.ogm.session.result.Result;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ArgumentNodeRepository extends GraphRepository<ArgumentNode> {

    @Query("START t=node({0}), r=node({1}) MATCH p=t<-[:SUPPORTED_BY|INTERPRETS*0..]-r RETURN p")
    Iterable<Map<String, Object>> getPaths(long childId, long rootId);

    //@Query("start n=node({0}) match n-[support:SUPPORTED_BY|INTERPRETS*0..5]->(argument:ArgumentNode) return argument")
    @Query("match path=(n:ArgumentNode {stableId: {0}})-[support:SUPPORTED_BY|INTERPRETS*0..5]->(argument:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody) return path")
    Set<ArgumentNode> getGraph(String stableRootId);

    @Query("start n=node({0}) match n-[:VERSION_OF]->(mv:MajorVersion) with mv match p=mv<-[:VERSION_OF]-(:ArgumentBody)" +
            "<-[:DEFINED_BY]-(node:ArgumentNode) return p")
    Set<ArgumentNode> getBodyChoices(long bodyId);

    @Query("match (node:ArgumentNode) return node")
    Set<ArgumentNode> getAllNodes();

    @Query("match p=(node:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody)-[:AUTHORED_BY]->(:User) return p")
    Set<ArgumentNode> getAllNodesRich();

    @Query("match p=(n:AssertionNode)-[:DEFINED_BY]->(:ArgumentBody)-[:AUTHORED_BY]->(:User) where not (:AssertionNode)-[:SUPPORTED_BY]->(n) return p")
    Set<ArgumentNode> getRootNodesRich();
}
