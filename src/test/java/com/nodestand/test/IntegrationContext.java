package com.nodestand.test;


import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Profile;
import org.springframework.data.neo4j.config.Neo4jConfiguration;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@org.springframework.context.annotation.Configuration
@EnableNeo4jRepositories("com.nodestand.nodes")
@EnableTransactionManagement
@ComponentScan("com.nodestand.service")
@Profile("integration")
public class IntegrationContext extends Neo4jConfiguration {

    @Override
    public SessionFactory getSessionFactory() {
        Configuration config = new Configuration();
        config.driverConfiguration()
                .setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
        return new SessionFactory(config, "com.nodestand.nodes");
    }

    @Override
    @Bean
    public Session getSession() throws Exception {
        return super.getSession();
    }

}
