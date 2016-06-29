package com.nodestand.nodes.repository;

import com.nodestand.nodes.comment.Commentable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import java.util.Set;

public interface CommentableRepository extends GraphRepository<Commentable> {

    @Query("start mv=node({0}) match path=mv<-[:RESPONDS_TO*0..]-(:Comment)-[:AUTHORED_BY]->author return path")
    Set<Commentable> getComments(long majorVersionId);
}
