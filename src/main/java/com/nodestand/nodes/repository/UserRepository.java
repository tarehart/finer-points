package com.nodestand.nodes.repository;

import com.nodestand.nodes.Author;
import com.nodestand.nodes.User;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface UserRepository extends GraphRepository<User> {

    @Query("match p=(u:User {stableId: {0}})<-[:CONTROLLED_BY]-(:Author) return p")
    User getUser(String stableId);

    @Query("match p=(a:Author {displayName: {0}})-[:CONTROLLED_BY]->(:User) return p")
    User findByAlias(String displayName);

    @Query("match p=(u:User {providerId: {0}, providerUserId: {1}})<-[:CONTROLLED_BY]-(:Author) return p")
    User findByConnectionKey(String providerId, String providerUserId);

    @Query("match p=(:Author {stableId: {0}})-[:CONTROLLED_BY]->(:User) return p")
    Author loadAuthor(String authorStableId);
}
