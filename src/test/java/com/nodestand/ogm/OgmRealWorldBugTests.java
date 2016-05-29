package com.nodestand.ogm;

import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationBody;
import com.nodestand.nodes.interpretation.InterpretationNode;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.neo4j.ogm.session.Session;
import org.neo4j.ogm.session.SessionFactory;
import org.neo4j.ogm.testutil.MultiDriverTestClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;


public class OgmRealWorldBugTests extends MultiDriverTestClass {


    private Session session;

    @Before
    public void init() throws IOException {
        session = new SessionFactory("com.nodestand.nodes").openSession();
    }

    /**
     * https://github.com/neo4j/neo4j-ogm/issues/93
     */
    @Ignore // currently broken
    @Test
    public void shouldLoadAllRelatedEntitiesWhenLoadingSuperclasses() {
        AssertionBody assertionBody = new AssertionBody();
        InterpretationBody interpretationBody = new InterpretationBody();
        AssertionNode childA = new AssertionNode(assertionBody);
        InterpretationNode childB = new InterpretationNode(interpretationBody);
        session.save(childA);
        session.save(childB);

        List<ArgumentNode> allNodes = new ArrayList<>();
        allNodes.add(childA);
        allNodes.add(childB);

        session.clear();

        Collection<ArgumentNode> all = session.loadAll(allNodes, 3);

        boolean foundInterp = false;

        for (ArgumentNode parentClass : all) {
            if (parentClass instanceof InterpretationNode) {
                foundInterp = true;
                break;
            }
        }

        assertTrue(foundInterp);
    }
}
