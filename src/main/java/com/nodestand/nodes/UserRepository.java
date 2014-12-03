package com.nodestand.nodes;

import org.springframework.data.neo4j.repository.GraphRepository;

public interface UserRepository extends GraphRepository<User> {
}
