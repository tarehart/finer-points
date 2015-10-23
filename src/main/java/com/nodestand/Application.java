package com.nodestand;

import org.neo4j.ogm.session.SessionFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.data.neo4j.server.RemoteServer;
import org.springframework.data.neo4j.template.Neo4jOperations;
import org.springframework.data.neo4j.template.Neo4jTemplate;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;

@SpringBootApplication
@EnableConfigurationProperties
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableNeo4jRepositories(basePackages = "com.nodestand.nodes")
public class Application extends Neo4jConfiguration {

    public Application() {
    }

    @Override
    public Neo4jServer neo4jServer() {
        return new RemoteServer("http://neo4j:pw@localhost:7474");
    }

    @Override
    public SessionFactory getSessionFactory() {
        return new SessionFactory("com.nodestand.nodes");
    }

    @Bean
    public Neo4jOperations neo4jOperations() throws Exception {
        return new Neo4jTemplate(getSession());
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}