package com.nodestand;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.data.neo4j.server.RemoteServer;

@Configuration
@EnableNeo4jRepositories(basePackages = "com.nodestand.nodes")
public class Neo4jConfig extends Neo4jConfiguration {

    @Autowired
    private Environment environment;

    public Neo4jConfig() {
    }

    @Override
    public Neo4jServer neo4jServer() {

        String url = environment.getProperty("neo4jUrl");
        String username = environment.getProperty("neo4jUsername");
        String password = environment.getProperty("neo4jPassword");

        return new RemoteServer(url, username, password);
    }

    @Override
    public SessionFactory getSessionFactory() {
        return new SessionFactory("com.nodestand.nodes");
    }

    @Bean
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Session getSession() throws Exception {
        return super.getSession();
    }

}