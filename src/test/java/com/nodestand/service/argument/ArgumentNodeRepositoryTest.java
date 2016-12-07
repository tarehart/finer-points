package com.nodestand.service.argument;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.service.user.UserService;
import com.nodestand.test.Neo4jIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.response.model.QueryResultModel;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
public class ArgumentNodeRepositoryTest extends Neo4jIntegrationTest {

    @Autowired
    private ArgumentService argumentService;

    @Autowired
    private ArgumentNodeRepository argumentNodeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private Session session;

    private Author registerUser(String socialId, String name) {

        final User user = new User(
                "google",
                socialId,
                User.Roles.ROLE_USER);

        Author author = user.addNewAlias(name);

        userRepository.save(user);

        return author;
    }

    @Test
    public void singlePathTest() throws NotAuthorizedException, NodeRulesException {

        Author jim = registerUser("1234", "Jim");

        AssertionNode triple = ArgumentTestUtil.createPublishedTriple(argumentService, jim);

        InterpretationNode interp = (InterpretationNode) triple.getGraphChildren().iterator().next();
        SourceNode source = interp.getSource();

        session.clear();

        QueryResultModel result = (QueryResultModel) argumentNodeRepository.getPaths(source.getId(), triple.getId());

        Assert.assertNotNull(result);

        Iterable<Map<String, Object>> results = result.queryResults();
        Assert.assertNotNull(results);

        Map<String, Object> firstResult = results.iterator().next();
        Assert.assertNotNull(firstResult);

        List<ArgumentNode> path = (List<ArgumentNode>) firstResult.get("path");
        Assert.assertNotNull(path);

        Assert.assertEquals(3, path.size());
        Assert.assertEquals(source, path.get(0));
        Assert.assertEquals(interp, path.get(1));
        Assert.assertEquals(triple, path.get(2));

        Assert.assertEquals(1, triple.getSupportingNodes().size());
        Assert.assertNotNull(triple.getBody());
        Assert.assertNotNull(triple.getBody().getMajorVersion());
    }


}
