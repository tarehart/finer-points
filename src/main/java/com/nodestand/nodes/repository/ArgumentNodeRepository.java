package com.nodestand.nodes.repository;

import com.nodestand.nodes.ArgumentNode;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface ArgumentNodeRepository extends GraphRepository<ArgumentNode> {

    @Query("START t=node({0}), r=node({1}) MATCH p=t<-[:SUPPORTED_BY|INTERPRETS*0..]-r RETURN p")
    Iterable<EntityPath<ArgumentNode, ArgumentNode>> getPaths(long childId, long rootId);
}
