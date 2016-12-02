package com.nodestand.test;

import com.nodestand.service.Neo4jDatabaseCleaner;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {IntegrationContext.class})
@ActiveProfiles({"integration"})
public class Neo4jIntegrationTest {

    @Autowired
    Neo4jDatabaseCleaner cleaner;

    @After
    public void cleanup() {
        cleaner.cleanDb();
    }

}
