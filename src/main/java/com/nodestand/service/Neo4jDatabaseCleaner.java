package com.nodestand.service;

import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class Neo4jDatabaseCleaner {

    @Autowired
    Session session;

    public void cleanDb() {
        session.purgeDatabase();
    }
}
