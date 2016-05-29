package com.nodestand.service.argument;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.controllers.serial.EditResult;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.service.user.UserService;
import com.nodestand.test.Neo4jIntegrationTest;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@RunWith(SpringJUnit4ClassRunner.class)
public class ArgumentServiceTest extends Neo4jIntegrationTest {

    @Autowired
    private ArgumentService argumentService;

    @Autowired
    private UserService userService;

    @Autowired
    private Session session;

    private User registerUser(String socialId, String name) {
        return userService.register(socialId, name).getUser();
    }

    @Test
    public void createAssertionTest() throws NodeRulesException {

        User jim = registerUser("1234", "Jim");

        List<Long> links = new LinkedList<>();

        AssertionNode assertionNode = argumentService.createAssertion(jim.getNodeId(), "Test Title", "Test Qual",
                "Hello, world!", links);

        Assert.assertNotNull(assertionNode);
        Assert.assertFalse(assertionNode.isFinalized());
        Assert.assertFalse(assertionNode.getBody().isPublic());
        Assert.assertTrue(assertionNode.getBody().isEditable());
        Assert.assertEquals("Test Title", assertionNode.getBody().getTitle());
        Assert.assertEquals("Test Qual", assertionNode.getBody().getQualifier());
        Assert.assertEquals("Hello, world!", assertionNode.getBody().getBody());

    }

    @Test
    public void publishTest() throws NodeRulesException, NotAuthorizedException {

        AssertionNode assertionNode = createPublishedAssertion();

        Assert.assertTrue(assertionNode.getBody().isPublic());
        Assert.assertTrue(assertionNode.getBody().isEditable());
    }


    @Test
    public void editingPublishedNodeTest() throws NotAuthorizedException, NodeRulesException {

        User kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = createPublishedAssertion();

        try {
            argumentService.editAssertion(kyle.getNodeId(), assertionNode.getId(), "Title", "Q", "Body", new LinkedList<>());
            Assert.fail("Should have thrown an exception because you can't edit a published node directly.");
        } catch (NodeRulesException e) {
            // Good.
        }
    }

    @Test
    public void testEditingViaDraft() throws NotAuthorizedException, NodeRulesException {
        User kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = createPublishedAssertion();

        EditResult result = argumentService.makeDraft(kyle.getNodeId(), assertionNode.getId());

        Assert.assertNotEquals(assertionNode.getId(), result.getEditedNode().getId());
        Assert.assertEquals(result.getEditedNode().getId(), result.getGraph().getRootId());
        Assert.assertFalse(result.getEditedNode().getBody().isPublic());

        ArgumentNode child = assertionNode.getSupportingNodes().stream().findFirst().get();
        List<Long> links = new LinkedList<>();
        links.add(child.getId());
        String body = "New Body {{[" + child.getBody().getMajorVersion().getStableId() + "]link}}";
        AssertionNode edited = argumentService.editAssertion(kyle.getNodeId(), result.getEditedNode().getId(), "New Title", "New Qual", body, links);

        Assert.assertFalse(edited.getBody().isPublic());
        Assert.assertEquals(assertionNode.getId(), edited.getPreviousVersion().getId());

        AssertionNode resultingNode = (AssertionNode) argumentService.publishNode(kyle.getNodeId(), edited.getId());

        Assert.assertEquals(assertionNode.getId(), resultingNode.getId());
        Assert.assertEquals("New Title", resultingNode.getBody().getTitle());
        Assert.assertEquals("New Qual", resultingNode.getBody().getQualifier());
        Assert.assertEquals(body, resultingNode.getBody().getBody());
        Assert.assertTrue(resultingNode.getBody().isEditable());
        Assert.assertTrue(resultingNode.getBody().isPublic());
    }

    @Test
    public void theGAUNTLET() throws NotAuthorizedException, NodeRulesException {
        User kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = createPublishedAssertion();

        InterpretationNode interp = (InterpretationNode) assertionNode.getSupportingNodes().stream().findFirst().get();

        SourceNode source = interp.getSource();

        session.clear();

        EditResult result = argumentService.makeDraft(kyle.getNodeId(), source.getId());

        Assert.assertNotEquals(source.getId(), result.getEditedNode().getId());

        ArgumentNode resultRoot = result.getGraph().getNodes().stream().filter(n -> Objects.equals(n.getId(), result.getGraph().getRootId())).findFirst().get();
        Assert.assertEquals(source.getId(), resultRoot.getPreviousVersion().getId());
        Assert.assertFalse(result.getEditedNode().getBody().isPublic());

        session.clear();

        SourceNode edited = argumentService.editSource(kyle.getNodeId(), result.getEditedNode().getId(), "New Title", "Q2", "new/url");

        Assert.assertFalse(edited.getBody().isPublic());
        Assert.assertEquals(source.getId(), edited.getPreviousVersion().getId());

        session.clear();

        SourceNode resultingNode = (SourceNode) argumentService.publishNode(kyle.getNodeId(), edited.getId());

        Assert.assertEquals(source.getId(), resultingNode.getId());
        Assert.assertEquals("New Title", resultingNode.getBody().getTitle());
        Assert.assertEquals("Q2", resultingNode.getBody().getQualifier());
        Assert.assertEquals("new/url", resultingNode.getBody().getUrl());
        Assert.assertTrue(resultingNode.getBody().isEditable());
        Assert.assertTrue(resultingNode.getBody().isPublic());

        session.clear();


        // Make sure the interpretation points to the edited source
        QuickGraphResponse interpGraph =  argumentService.getGraph(interp.getStableId());
        Assert.assertEquals(resultingNode.getId(), ((InterpretationNode)interpGraph.getRootNode()).getSource().getId());
        session.clear();

        // Make the interpretation a draft
        EditResult interpDraftResult = argumentService.makeDraft(kyle.getNodeId(), interp.getId());

        // Make sure the interpretation draft still points to the source
        InterpretationNode interpDraft = (InterpretationNode)interpDraftResult.getEditedNode();
        Assert.assertEquals(resultingNode.getId(), interpDraft.getSource().getId());
        session.clear();

        // Edit the interp draft
        argumentService.editInterpretation(kyle.getNodeId(), interpDraft.getId(), "Edited interp", "Q2", "Edited interp body", resultingNode.getId());
        session.clear();

        // Make the assertion a draft
        EditResult assertionDraftResult = argumentService.makeDraft(kyle.getNodeId(), assertionNode.getId());
        AssertionNode assertionDraft = (AssertionNode) assertionDraftResult.getEditedNode();

        // Make sure the assertion points to the interp original, not the interp draft
        Assert.assertEquals(1, assertionDraft.getSupportingNodes().size());
        InterpretationNode child = (InterpretationNode) assertionDraft.getSupportingNodes().stream().findFirst().get();
        Assert.assertEquals(interp.getId(), child.getId());
        Assert.assertNotEquals(interpDraft.getId(), child.getId());
        session.clear();

        // Publish the interp draft
        InterpretationNode publishedInterp = (InterpretationNode) argumentService.publishNode(kyle.getNodeId(), interpDraft.getId());
        session.clear();

        // Make sure the original assertion points to the published changed interp
        QuickGraphResponse latestTree = argumentService.getGraph(assertionNode.getStableId());
        InterpretationNode treeInterp = (InterpretationNode) latestTree.getRootNode().getGraphChildren().stream().findFirst().get();
        Assert.assertEquals(publishedInterp.getId(), treeInterp.getId());
        Assert.assertEquals(publishedInterp.getBody().getTitle(), treeInterp.getBody().getTitle());
        Assert.assertEquals("Edited interp", publishedInterp.getBody().getTitle());
        session.clear();

        // Make sure the draft assertion does the same
        QuickGraphResponse draftTree = argumentService.getGraph(assertionDraft.getStableId());
        treeInterp = (InterpretationNode) draftTree.getRootNode().getGraphChildren().stream().findFirst().get();
        Assert.assertEquals(publishedInterp.getId(), treeInterp.getId());
        session.clear();

        // Edit and publish the assertion
        List<Long> links = new LinkedList<>();
        links.add(publishedInterp.getId());
        argumentService.editAssertion(kyle.getNodeId(), assertionDraft.getId(), "Edited assertion", "Q2", "Edited assertion body " + assertionNode.getBody().getBody(), links);
        session.clear();
        AssertionNode publishedAssertion = (AssertionNode) argumentService.publishNode(kyle.getNodeId(), assertionDraft.getId());
        session.clear();

        // Make sure the resulting graph looks good
        QuickGraphResponse finalTree = argumentService.getGraph(publishedAssertion.getStableId());
        Assert.assertEquals(assertionNode.getId(), finalTree.getRootId());

        // Make sure the drafts are all gone
        Assert.assertNull(argumentService.getFullDetail(assertionDraft.getId()));
        Assert.assertNull(argumentService.getFullDetail(interpDraft.getId()));
        Assert.assertNull(argumentService.getFullDetail(edited.getId()));
    }

    @Test
    public void testChildDraftCreation() throws NotAuthorizedException, NodeRulesException {
        User kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = createPublishedAssertion();

        InterpretationNode interp = (InterpretationNode) assertionNode.getGraphChildren().iterator().next();
        SourceNode sourceNode = interp.getSource();

        EditResult result = argumentService.makeDraft(kyle.getNodeId(), sourceNode.getId());

        Assert.assertEquals(sourceNode.getId(), result.getEditedNode().getPreviousVersion().getId()); // The draft node shows the original as its previous version.
        Assert.assertEquals(result.getEditedNode().getId(), result.getGraph().getRootId()); // The root was the subject of editing
        Assert.assertFalse(result.getEditedNode().getBody().isPublic());
    }

    @Test
    public void testPublishWithSessionClears() throws NodeRulesException, NotAuthorizedException {

        User jim = registerUser("1234", "Jim");
        List<Long> links = new LinkedList<>();

        AssertionNode assertionNode = argumentService.createAssertion(jim.getNodeId(), "Assertion Title", "Original",
                "Hello, world!", links);
        session.clear();

        InterpretationNode interpretationNode = argumentService.createInterpretation(jim.getNodeId(), "Interp Title",
                "Original", "Interp body", null);
        session.clear();

        // Edit the assertion to point to the interpretation
        links.add(interpretationNode.getId());
        assertionNode = argumentService.editAssertion(jim.getNodeId(), assertionNode.getId(), "Assertion Title", "Q", "Hello! {{[" +
                interpretationNode.getBody().getMajorVersion().getStableId() + "]link}}", links);
        session.clear();

        SourceNode sourceNode = argumentService.createSource(jim.getNodeId(), "Source Title", "Original", "http://google.com");
        session.clear();

        argumentService.editInterpretation(jim.getNodeId(), interpretationNode.getId(), "Interp Title", "Q2", "Interp body", sourceNode.getId());
        session.clear();

        AssertionNode published = (AssertionNode) argumentService.publishNode(jim.getNodeId(), assertionNode.getId());

        Assert.assertTrue(published.getBody().isPublic());
    }




    private AssertionNode createPublishedAssertion() throws NodeRulesException, NotAuthorizedException {

        User jim = registerUser("1234", "Jim");
        return ArgumentTestUtil.createPublishedTriple(argumentService, jim);
    }


}
