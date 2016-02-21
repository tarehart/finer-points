package com.nodestand.service.argument;

import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.test.Neo4jIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedList;
import java.util.List;


public class ArgumentServiceTest extends Neo4jIntegrationTest {

    @Autowired
    private ArgumentService argumentService;

    @Autowired
    UserRepository userRepository;

    private User insertUser() {
        User user = new User("1234", "Jim", User.Roles.ROLE_USER);
        userRepository.save(user);
        return user;
    }

    @Test
    public void CreateAssertionTest() {

        User user = insertUser();

        List<Long> links = new LinkedList<>();

        AssertionNode assertionNode = argumentService.createAssertion(user.getNodeId(), "Title", "body", links);

        Assert.assertNotNull(assertionNode);

    }


}
