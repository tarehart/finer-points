package com.nodestand.nodes.repository;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.neo4j.repository.GraphRepository;
import org.springframework.data.neo4j.repository.NodeGraphRepositoryImpl;
import org.springframework.data.neo4j.support.Neo4jTemplate;
import org.springframework.stereotype.Component;

/**
 * I've made this a concrete class in order to get access to the findAllByQuery(String, String, Object) method. The
 * normal thing to do would be to create an interface that extends GraphRepository<ArgumentBody>.
 */
public class ArgumentBodyRepository extends NodeGraphRepositoryImpl<ArgumentBody> {

    public ArgumentBodyRepository(Neo4jTemplate template) {
        super(ArgumentBody.class, template);
    }
}
