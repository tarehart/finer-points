package com.nodestand;

import com.nodestand.service.NodeUserDetailsServiceImpl;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.social.connect.UsersConnectionRepository;

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

    @Bean
    NodeUserDetailsService nodeUserDetailsService() {
        return new NodeUserDetailsServiceImpl();
    }

//    @Autowired
//    private UsersConnectionRepository usersConnectionRepository;

//    @Bean
//    UserInterceptor userInterceptor() {
//        return new UserInterceptor(usersConnectionRepository);
//    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}