package com.nodestand.nodes.repository;

import com.nodestand.nodes.comment.Commentable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import java.util.Set;

public interface CommentableRepository extends GraphRepository<Commentable> {

    @Query("start n=node({0}) match p=n-[:VERSION_OF]->(mv:MajorVersion) " +
            "with mv match mv<-[:VERSION_OF]-(argBody:ArgumentBody)<-[resp:RESPONDS_TO*0..]-(node:Commentable)-[:AUTHORED_BY]->author " +
            "return p")
    Set<Commentable> getComments(long bodyNodeId);
}
