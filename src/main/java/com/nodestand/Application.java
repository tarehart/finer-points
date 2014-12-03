package com.nodestand;

import com.nodestand.nodes.NodeUserDetailImpl;
import com.nodestand.service.NodeUserDetailsService;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.config.EnableNeo4jRepositories;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.dao.SystemWideSaltSource;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;

@ComponentScan
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
    Md5PasswordEncoder passwordEncoder() {
        return new Md5PasswordEncoder();
    }

    @Bean
    SystemWideSaltSource saltSource() {
        SystemWideSaltSource s = new SystemWideSaltSource();
        s.setSystemWideSalt("woifmdcvnm");
        return s;
    }

    @Bean
    NodeUserDetailsService nodeUserDetailsService() {
        return new NodeUserDetailImpl();
    }

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}