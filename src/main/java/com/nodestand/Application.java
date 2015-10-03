package com.nodestand;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@ComponentScan
@EnableConfigurationProperties
@EnableAutoConfiguration
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableNeo4jRepositories(basePackages = "com.nodestand.nodes")
public class Application extends Neo4jConfiguration {

    @Autowired
    private Neo4jServer neo4jServer;

    @Autowired
    private SessionFactory sessionFactory;

    public Application() {
    }

    @Override
    public Neo4jServer neo4jServer() {
        return neo4jServer;
    }

    @Override
    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Bean
    GraphDatabaseService graphDatabaseService() {
        return new GraphDatabaseFactory().newEmbeddedDatabase("assertions.db");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}