package com.nodestand.service.argument;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.controllers.serial.EditResult;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.service.user.UserService;
import com.nodestand.test.Neo4jIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.model.Result;
import org.neo4j.ogm.response.model.QueryResultModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RunWith(SpringJUnit4ClassRunner.class)
public class ArgumentNodeRepositoryTest extends Neo4jIntegrationTest {

    @Autowired
    private ArgumentService argumentService;

    @Autowired
    private ArgumentNodeRepository argumentNodeRepository;

    @Autowired
    private UserService userService;

    private User registerUser(String socialId, String name) {
        return userService.register(socialId, name).getUser();
    }

    @Test
    public void singlePathTest() throws NotAuthorizedException, NodeRulesException {

        User jim = registerUser("1234", "Jim");

        AssertionNode triple = ArgumentTestUtil.createPublishedTriple(argumentService, jim);

        InterpretationNode interp = (InterpretationNode) triple.getGraphChildren().iterator().next();
        SourceNode source = interp.getSource();
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
    }


}
