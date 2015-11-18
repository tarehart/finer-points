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
    @Query("start n=node({0}) match path=n-[support:SUPPORTED_BY|INTERPRETS*0..5]->(argument:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody) return path")
    Set<ArgumentNode> getGraph(long rootId);

    @Query("start n=node({0}) match p=(build:Build)<-[:BUILT_BY]-consumer-[:SUPPORTED_BY|INTERPRETS*1]->n return p")
    Set<ArgumentNode> getConsumers(long nodeId);

    @Query("start n=node({0}) match n-[:VERSION_OF]->(mv:MajorVersion) with mv match p=mv<-[:VERSION_OF]-(:ArgumentBody)" +
            "<-[:DEFINED_BY]-(node:ArgumentNode) return p")
    Set<ArgumentNode> getBodyChoices(long bodyId);

    @Query("match (node:ArgumentNode) return node")
    Set<ArgumentNode> getAllNodes();
}
