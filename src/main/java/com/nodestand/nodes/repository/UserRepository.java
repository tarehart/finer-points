package com.nodestand.nodes.repository;

import com.nodestand.nodes.User;
import org.springframework.data.neo4j.repository.GraphRepository;

public interface UserRepository extends GraphRepository<User> {
}
