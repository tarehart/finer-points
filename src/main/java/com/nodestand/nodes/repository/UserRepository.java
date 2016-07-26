package com.nodestand.nodes.repository;

import com.nodestand.nodes.User;
import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface UserRepository extends GraphRepository<User> {

    @Query("match (u:User {stableId: {0}}) return u")
    User getUser(String stableId);

    @Query("match (u:User {displayName: {0}}) return u")
    User findByUsername(String username);

    @Query("match (u:User {providerId: {0}, providerUserId: {1}}) return u")
    User findByConnectionKey(String providerId, String providerUserId);
}
