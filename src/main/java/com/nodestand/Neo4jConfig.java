package com.nodestand;

import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.server.Neo4jServer;
import org.springframework.data.neo4j.server.RemoteServer;

@Configuration
@EnableNeo4jRepositories(basePackages = "com.nodestand.nodes")
public class Neo4jConfig extends Neo4jConfiguration {

    public Neo4jConfig() {
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
    @Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
    public Session getSession() throws Exception {
        return super.getSession();
    }

}