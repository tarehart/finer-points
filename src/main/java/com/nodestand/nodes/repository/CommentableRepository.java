package com.nodestand.nodes.repository;

import com.nodestand.nodes.comment.Comment;
import com.nodestand.nodes.comment.Commentable;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

import java.util.Set;

public interface CommentableRepository extends GraphRepository<Commentable> {

    @Query("match path=(:ArgumentNode {stableId: {0}})-[:DEFINED_BY]->(:ArgumentBody)-[:VERSION_OF]->(:MajorVersion)" +
            "<-[:RESPONDS_TO*0..]-(:Comment)-[:AUTHORED_BY]->(:Author) return path")
    Set<Commentable> getComments(String nodeStableId);

    @Query("match p=(c:Comment)-[:AUTHORED_BY]->(:Author)-[:CONTROLLED_BY]->(:User)" +
            " where ID(c) = {0} return p")
    Comment loadWithAuthor(long commentId);
}
