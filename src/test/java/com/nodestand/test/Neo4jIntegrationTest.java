package com.nodestand.test;

import com.nodestand.service.Neo4jDatabaseCleaner;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@ContextConfiguration(classes = {IntegrationContext.class})
@RunWith(SpringJUnit4ClassRunner.class)
@ActiveProfiles({"integration"})
public class Neo4jIntegrationTest {

    @Autowired
    Neo4jDatabaseCleaner cleaner;

    @After
    public void cleanup() {
        cleaner.cleanDb();
    }

}
