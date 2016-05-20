package com.nodestand.nodes.repository;

import com.nodestand.nodes.ArgumentNode;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.QueryResultModel;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import java.util.Map;
import java.util.Set;

public interface ArgumentNodeRepository extends GraphRepository<ArgumentNode> {

    @Query("START t=node({0}), r=node({1}) MATCH p=t<-[:SUPPORTED_BY|INTERPRETS*0..]-r " +
            "WITH p as p, nodes(p) as path UNWIND path AS item MATCH mvPath=item-[:DEFINED_BY]->(:ArgumentBody)-[:VERSION_OF]->(:MajorVersion) " +
            "RETURN path, rels(p), nodes(mvPath), rels(mvPath)")
    Result getPaths(long childId, long rootId);

    @Query("match path=(n:ArgumentNode {stableId: {0}})-[support:SUPPORTED_BY|INTERPRETS*0..5]->(argument:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody) return path")
    Set<ArgumentNode> getGraph(String stableRootId);

    @Query("start mv=node({0}) match p=mv<-[:VERSION_OF]-(:ArgumentBody)" +
            "<-[:DEFINED_BY]-(node:ArgumentNode) return p")
    Set<ArgumentNode> getNodesInMajorVersion(long majorVersionId);

    @Query("match (node:ArgumentNode) return node")
    Set<ArgumentNode> getAllNodes();

    @Query("match p=(node:ArgumentNode)-[:DEFINED_BY]->(body:ArgumentBody)-[:AUTHORED_BY]->(:User) return p")
    Set<ArgumentNode> getAllNodesRich();

    @Query("match p=(n:AssertionNode)-[:DEFINED_BY]->(:ArgumentBody {isPublic:true})-[:AUTHORED_BY]->(:User) where not (:AssertionNode)-[:SUPPORTED_BY]->(n) return p")
    Set<ArgumentNode> getRootNodesRich();

    @Query("start n=node({0}) match body-[VERSION_OF]->n return max(body.minorVersion)")
    Integer getMaxMinorVersion(long majorVersionId);


    @Query("start n=node({0}) match node-[DEFINED_BY]->n return max(node.buildVersion)")
    Integer getMaxBuildVersion(long bodyId);

    @Query("start n=node({0}) match n-[:SUPPORTED_BY*0..]->(support:ArgumentNode) " +
            "WHERE NOT support-[:INTERPRETS]->(:SourceNode) AND NOT support-[:SUPPORTED_BY]->(:ArgumentNode) return support")
    Set<ArgumentNode> getUnsupportedNodes(long nodeId);

    @Query("start n=node({0}) match p=n-[:DEFINED_BY]->(:ArgumentBody)-[:VERSION_OF]->(:MajorVersion) return p")
    ArgumentNode loadWithMajorVersion(long id);

    @Query("start u=node({0}) match p=u<-[:AUTHORED_BY]-(b:ArgumentBody)<-[:DEFINED_BY]-(n:ArgumentNode) where not b.isPublic return p")
    Set<ArgumentNode> getDraftNodesRich(long userId);

    @Query("start n=node({0}) match (c:ArgumentNode)-[:SUPPORTED_BY|INTERPRETS]->n with c match p=c-[:DEFINED_BY]->(b:ArgumentBody)-[:AUTHORED_BY]->(:User) where b.isPublic return p")
    Set<ArgumentNode> getConsumerNodes(long nodeId);

    @Query("start n=node({0}), u=node({1}) match (c:ArgumentNode)-[:SUPPORTED_BY|INTERPRETS]->n with c, u match p=c-[:DEFINED_BY]->(b:ArgumentBody)-[:AUTHORED_BY]->(a:User) where b.isPublic or a = u return p")
    Set<ArgumentNode> getConsumerNodes(long nodeId, long userId);
}
