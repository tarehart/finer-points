package com.nodestand.service.vote;

import com.nodestand.controllers.serial.EditResult;
import com.nodestand.nodes.*;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.nodes.vote.VoteType;
import com.nodestand.service.argument.ArgumentService;
import com.nodestand.service.argument.ArgumentTestUtil;
import com.nodestand.test.Neo4jIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Tyler on 1/2/2017.
 */
@RunWith(SpringJUnit4ClassRunner.class)
public class VoteServiceNeo4jTest extends Neo4jIntegrationTest {

    @Autowired
    private VoteService voteService;

    @Autowired
    private ArgumentService argumentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Session session;

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private Author firstAuthor;
    private Author secondAuthor;
    private Author bystander;
    private AssertionNode rootNode;

    @Before
    public void setup() throws NodeRulesException {
        firstAuthor = ArgumentTestUtil.registerUser(userRepository, "123", "Taylor");
        secondAuthor = ArgumentTestUtil.registerUser(userRepository, "456", "Hector");
        bystander = ArgumentTestUtil.registerUser(userRepository, "789", "Maurice");
        rootNode = ArgumentTestUtil.createPublishedTriple(argumentService, firstAuthor);

    }

    @Test
    public void voteOwnNode() throws Exception {
        thrown.expect(NodeRulesException.class); // Can't vote on own node.
        voteService.voteNode(firstAuthor.getUser().getStableId(), rootNode.getStableId(), VoteType.GREAT);
    }

    @Test
    public void voteNode() throws Exception {

        voteService.voteNode(bystander.getUser().getStableId(), rootNode.getStableId(), VoteType.GREAT);

        session.clear();

        Author fo = userRepository.loadAuthor(firstAuthor.getStableId());
        Assert.assertEquals(10L, fo.getNodePoints());
    }

    @Test
    public void unvoteNode() throws Exception {
        voteNode();

        voteService.unvoteNode(rootNode.getStableId(), bystander.getUser().getStableId());

        Author fo = userRepository.loadAuthor(firstAuthor.getStableId());

        Assert.assertEquals(0L, fo.getNodePoints());
    }

    @Test
    public void voteSplit() throws Exception {

        voteNode();

        ArgumentNode child = ArgumentTestUtil.createPublishedTriple(argumentService, firstAuthor);
        EditResult editResult = argumentService.makeDraft(secondAuthor.getUser().getNodeId(), secondAuthor.getStableId(), rootNode.getId());

        List<Long> links = editResult.getEditedNode().getGraphChildren().stream().map(Node::getId).collect(Collectors.toList());
        links.add(child.getId());

        String body = rootNode.getBody().getBody() + " {{[" + child.getBody().getMajorVersion().getStableId() +
                "]hello}}";
        argumentService.editAssertion(secondAuthor.getUser().getNodeId(), editResult.getEditedNode().getId(),
                "title", "qual", body, links);

        argumentService.publishNode(secondAuthor.getUser().getNodeId(), editResult.getEditedNode().getId());

        session.clear();

        voteService.voteNode(bystander.getUser().getStableId(), rootNode.getStableId(), VoteType.WEAK);

        // Both first and second author suffer a revocation of a GREAT, and both get points for a WEAK.

        Author fo = userRepository.loadAuthor(firstAuthor.getStableId());
        Assert.assertEquals(2L, fo.getNodePoints());

        Author so = userRepository.loadAuthor(secondAuthor.getStableId());
        Assert.assertEquals(-8L, so.getNodePoints());

    }

    private User freshUser(Author author, UserRepository userRepository) {
        return userRepository.getUser(author.getUser().getStableId());
    }

    @Test
    public void voteChange() throws Exception {

        voteService.voteNode(bystander.getUser().getStableId(), rootNode.getStableId(), VoteType.GREAT);

        session.clear();

        voteService.voteNode(bystander.getUser().getStableId(), rootNode.getStableId(), VoteType.WEAK);

        session.clear();

        Author fo = userRepository.loadAuthor(firstAuthor.getStableId());
        Assert.assertEquals(2L, fo.getNodePoints());
    }

    @Test
    public void noGivingSelfPoints() throws Exception {

        voteNode();

        ArgumentNode child = ArgumentTestUtil.createPublishedTriple(argumentService, firstAuthor);
        EditResult editResult = argumentService.makeDraft(secondAuthor.getUser().getNodeId(), secondAuthor.getStableId(), rootNode.getId());

        List<Long> links = editResult.getEditedNode().getGraphChildren().stream().map(Node::getId).collect(Collectors.toList());
        links.add(child.getId());

        String body = rootNode.getBody().getBody() + " {{[" + child.getBody().getMajorVersion().getStableId() +
                "]hello}}";
        argumentService.editAssertion(secondAuthor.getUser().getNodeId(), editResult.getEditedNode().getId(),
                "title", "qual", body, links);

        argumentService.publishNode(secondAuthor.getUser().getNodeId(), editResult.getEditedNode().getId());

        session.clear();

        voteService.voteNode(secondAuthor.getUser().getStableId(), rootNode.getStableId(), VoteType.GREAT);

        Author fo = userRepository.loadAuthor(firstAuthor.getStableId());
        Assert.assertEquals(20L, fo.getNodePoints());

        // Second author is not allowed to give himself points so he gets nothing.
        Author so = userRepository.loadAuthor(secondAuthor.getStableId());
        Assert.assertEquals(0L, so.getNodePoints());


    }

}