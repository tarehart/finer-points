package com.nodestand;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;

@ComponentScan
@EnableConfigurationProperties
@EnableAutoConfiguration
@Configuration
@EnableNeo4jRepositories(basePackages = "com.nodestand.nodes")
public class Application extends Neo4jConfiguration {

    public Application() {
        setBasePackage("com.nodestand.nodes");
    }

    @Bean
    GraphDatabaseService graphDatabaseService() {
        return new GraphDatabaseFactory().newEmbeddedDatabase("assertions.db");
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}