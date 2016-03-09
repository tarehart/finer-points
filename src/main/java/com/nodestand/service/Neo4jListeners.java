package com.nodestand.service;

import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.neo4j.event.BeforeSaveEvent;

@Configuration
public class Neo4jListeners {

    @Autowired
    private ArgumentNodeRepository argumentNodeRepository;

    @Bean
    ApplicationListener<BeforeSaveEvent> beforeSaveEventApplicationListener() {

        return new ApplicationListener<BeforeSaveEvent>() {
            @Override
            public void onApplicationEvent(BeforeSaveEvent event) {
                Object entity = event.getEntity();
                if (entity instanceof AssertionNode) {
                    try {
                        ((AssertionNode) entity).updateChildOrder(argumentNodeRepository);
                    } catch (NodeRulesException e) {
                        throw new RuntimeException("Assertion node's children are in a bad state!", e);
                    }
                }
            }
        };
    }

}
