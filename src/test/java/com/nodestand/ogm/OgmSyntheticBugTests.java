package com.nodestand.ogm;

import com.nodestand.domain.Candidate;
import com.nodestand.domain.Voter;
import com.nodestand.nodes.NodeRulesException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;


public class OgmSyntheticBugTests {

    @Rule
    public Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

    private static final SessionFactory sessionFactory = new SessionFactory("com.nodestand.domain");

    private Session session;

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession(neo4jRule.url());
    }

    private GraphDatabaseService getDatabase() {
        return neo4jRule.getGraphDatabaseService();
    }

    @Test
    public void deleteIncomingRelationship() {
        Candidate candidate = new Candidate();
        Voter user = new Voter();

        candidate.voters = new HashSet<>();
        candidate.voters.add(user);
        session.save(candidate);
        session.clear();

        candidate = session.load(Candidate.class, candidate.id);
        candidate.voters.clear();
        session.save(candidate);
        session.clear();

        candidate = session.load(Candidate.class, candidate.id);

        assertTrue(candidate.voters == null || candidate.voters.size() == 0);
    }
}
