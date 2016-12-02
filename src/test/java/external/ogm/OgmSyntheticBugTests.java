package external.ogm;

import external.ogm.domain.ArrayHolder;
import external.ogm.domain.Candidate;
import external.ogm.domain.Voter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertTrue;


public class OgmSyntheticBugTests extends MultiDriverTestClass {

    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("external.ogm.domain").openSession();
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

    @Test
    public void loadIntArray() {
        ArrayHolder holder = new ArrayHolder();
        holder.intArr = new Integer[]{1};

        session.save(holder);
        session.clear();

        holder = session.load(ArrayHolder.class, holder.id);
        Assert.assertNotNull(holder.intArr);
        Assert.assertEquals(1, holder.intArr.length);
        Assert.assertEquals(1, (long) holder.intArr[0]);
    }

    @Ignore
    @Test
    public void queryIntArray() {
        ArrayHolder holder = new ArrayHolder();
        holder.intArr = new Integer[]{1};

        session.save(holder);
        session.clear();

        Map<String, Object> params = new HashMap<>();
        session.query("match n return n", params); // Throws exception
    }

    @Ignore
    @Test
    public void queryStringArray() {
        ArrayHolder holder = new ArrayHolder();
        holder.stringArr = new String[] {"hi"};

        session.save(holder);
        session.clear();

        Map<String, Object> params = new HashMap<>();
        session.query("match n return n", params); // Throws exception
        holder = session.load(ArrayHolder.class, holder.id);
        Assert.assertNotNull(holder.stringArr);
        Assert.assertEquals(1, holder.stringArr.length);
        Assert.assertEquals("hi", holder.stringArr[0]);
    }
}
