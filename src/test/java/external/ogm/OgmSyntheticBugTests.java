package external.ogm;

import external.ogm.domain.Candidate;
import external.ogm.domain.Voter;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.neo4j.ogm.config.Configuration;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.Neo4jIntegrationTestRule;

import java.io.IOException;
import java.util.HashSet;

import static org.junit.Assert.assertTrue;


public class OgmSyntheticBugTests {

    @Rule
    public Neo4jIntegrationTestRule neo4jRule = new Neo4jIntegrationTestRule();

    private static final SessionFactory sessionFactory;

    static {
        Configuration config = new Configuration();
        config.driverConfiguration()
                .setDriverClassName("org.neo4j.ogm.drivers.embedded.driver.EmbeddedDriver");
        sessionFactory = new SessionFactory(config, "com.nodestand.domain");
    }

    private Session session;

    @Before
    public void init() throws IOException {
        session = sessionFactory.openSession();
    }

    @Ignore
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
