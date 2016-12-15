package com.nodestand.service.argument;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.controllers.serial.EditResult;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.UserRepository;
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
import java.util.Set;

@RunWith(SpringJUnit4ClassRunner.class)
public class ArgumentServiceTest extends Neo4jIntegrationTest {

    @Autowired
    private ArgumentService argumentService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

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
    public void createAssertionTest() throws NodeRulesException {

        Author jim = registerUser("1234", "Jim");

        List<Long> links = new LinkedList<>();

        AssertionNode assertionNode = argumentService.createAssertion(jim.getUser().getNodeId(), jim.getStableId(), "Test Title", "Test Qual",
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

        Author kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = createPublishedAssertion();

        try {
            argumentService.editAssertion(kyle.getUser().getNodeId(), assertionNode.getId(), "Title", "Q", "Body", new LinkedList<>());
            Assert.fail("Should have thrown an exception because you can't edit a published node directly.");
        } catch (NodeRulesException e) {
            // Good.
        }
    }

    @Test
    public void testEditingViaDraft() throws NotAuthorizedException, NodeRulesException {
        Author kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = createPublishedAssertion();

        EditResult result = argumentService.makeDraft(kyle.getUser().getNodeId(), kyle.getStableId(), assertionNode.getId());

        Assert.assertNotEquals(assertionNode.getId(), result.getEditedNode().getId());
        Assert.assertEquals(result.getEditedNode().getId(), result.getGraph().getRootId());
        Assert.assertFalse(result.getEditedNode().getBody().isPublic());

        ArgumentNode child = assertionNode.getSupportingNodes().stream().findFirst().get();
        List<Long> links = new LinkedList<>();
        links.add(child.getId());
        String body = "New Body {{[" + child.getBody().getMajorVersion().getStableId() + "]link}}";
        AssertionNode edited = argumentService.editAssertion(kyle.getUser().getNodeId(), result.getEditedNode().getId(), "New Title", "New Qual", body, links);

        Assert.assertFalse(edited.getBody().isPublic());
        Assert.assertEquals(assertionNode.getId(), edited.getPreviousVersion().getId());

        AssertionNode resultingNode = (AssertionNode) argumentService.publishNode(kyle.getUser().getNodeId(), edited.getId()).getRootNode();

        Assert.assertEquals(assertionNode.getId(), resultingNode.getId());
        Assert.assertEquals("New Title", resultingNode.getBody().getTitle());
        Assert.assertEquals("New Qual", resultingNode.getBody().getQualifier());
        Assert.assertEquals(body, resultingNode.getBody().getBody());
        Assert.assertTrue(resultingNode.getBody().isEditable());
        Assert.assertTrue(resultingNode.getBody().isPublic());
    }

    @Test
    public void theGAUNTLET() throws NotAuthorizedException, NodeRulesException {
        Author kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = createPublishedAssertion();

        InterpretationNode interp = (InterpretationNode) assertionNode.getSupportingNodes().stream().findFirst().get();

        SourceNode source = interp.getSource();

        session.clear();

        EditResult result = argumentService.makeDraft(kyle.getUser().getNodeId(), kyle.getStableId(), source.getId());

        Assert.assertNotEquals(source.getId(), result.getEditedNode().getId());

        ArgumentNode resultRoot = result.getGraph().getNodes().stream().filter(n -> Objects.equals(n.getId(), result.getGraph().getRootId())).findFirst().get();
        Assert.assertEquals(source.getId(), resultRoot.getPreviousVersion().getId());
        Assert.assertFalse(result.getEditedNode().getBody().isPublic());

        session.clear();

        SourceNode edited = argumentService.editSource(kyle.getUser().getNodeId(), result.getEditedNode().getId(), "New Title", "Q2", "new/url");

        Assert.assertFalse(edited.getBody().isPublic());
        Assert.assertEquals(source.getId(), edited.getPreviousVersion().getId());

        session.clear();

        SourceNode resultingNode = (SourceNode) argumentService.publishNode(kyle.getUser().getNodeId(), edited.getId()).getRootNode();

        Assert.assertEquals(source.getId(), resultingNode.getId());
        Assert.assertEquals("New Title", resultingNode.getBody().getTitle());
        Assert.assertEquals("Q2", resultingNode.getBody().getQualifier());
        Assert.assertEquals("new/url", resultingNode.getBody().getUrl());
        Assert.assertTrue(resultingNode.getBody().isEditable());
        Assert.assertTrue(resultingNode.getBody().isPublic());

        session.clear();


        // Make sure the interpretation points to the edited source
        QuickGraphResponse interpGraph =  argumentService.getGraph(interp.getStableId(), kyle.getUser().getNodeId());
        Assert.assertEquals(resultingNode.getId(), ((InterpretationNode)interpGraph.getRootNode()).getSource().getId());
        session.clear();

        // Make the interpretation a draft
        EditResult interpDraftResult = argumentService.makeDraft(kyle.getUser().getNodeId(), kyle.getStableId(), interp.getId());

        // Make sure the interpretation draft still points to the source
        InterpretationNode interpDraft = (InterpretationNode)interpDraftResult.getEditedNode();
        Assert.assertEquals(resultingNode.getId(), interpDraft.getSource().getId());
        session.clear();

        // Edit the interp draft
        argumentService.editInterpretation(kyle.getUser().getNodeId(), interpDraft.getId(), "Edited interp", "Q2", "Edited interp body", resultingNode.getId());
        session.clear();

        // Make the assertion a draft
        EditResult assertionDraftResult = argumentService.makeDraft(kyle.getUser().getNodeId(), kyle.getStableId(), assertionNode.getId());
        AssertionNode assertionDraft = (AssertionNode) assertionDraftResult.getEditedNode();

        // Make sure the assertion points to the interp original, not the interp draft
        Assert.assertEquals(1, assertionDraft.getSupportingNodes().size());
        InterpretationNode child = (InterpretationNode) assertionDraft.getSupportingNodes().stream().findFirst().get();
        Assert.assertEquals(interp.getId(), child.getId());
        Assert.assertNotEquals(interpDraft.getId(), child.getId());
        session.clear();

        // Publish the interp draft
        InterpretationNode publishedInterp = (InterpretationNode) argumentService.publishNode(kyle.getUser().getNodeId(), interpDraft.getId()).getRootNode();
        session.clear();

        // Make sure the original assertion points to the published changed interp
        QuickGraphResponse latestTree = argumentService.getGraph(assertionNode.getStableId(), kyle.getUser().getNodeId());
        InterpretationNode treeInterp = (InterpretationNode) latestTree.getRootNode().getGraphChildren().stream().findFirst().get();
        Assert.assertEquals(publishedInterp.getId(), treeInterp.getId());
        Assert.assertEquals(publishedInterp.getStableId(), treeInterp.getStableId());
        Assert.assertEquals(publishedInterp.getBody().getTitle(), treeInterp.getBody().getTitle());
        Assert.assertEquals("Edited interp", publishedInterp.getBody().getTitle());
        session.clear();

        // Make sure the draft assertion does the same
        QuickGraphResponse draftTree = argumentService.getGraph(assertionDraft.getStableId(), kyle.getUser().getNodeId());
        treeInterp = (InterpretationNode) draftTree.getRootNode().getGraphChildren().stream().findFirst().get();
        Assert.assertEquals(publishedInterp.getId(), treeInterp.getId());
        session.clear();

        // Edit and publish the assertion
        List<Long> links = new LinkedList<>();
        links.add(publishedInterp.getId());
        argumentService.editAssertion(kyle.getUser().getNodeId(), assertionDraft.getId(), "Edited assertion", "Q2", "Edited assertion body " + assertionNode.getBody().getBody(), links);
        session.clear();
        AssertionNode publishedAssertion = (AssertionNode) argumentService.publishNode(kyle.getUser().getNodeId(), assertionDraft.getId()).getRootNode();
        session.clear();

        // Make sure the resulting graph looks good
        QuickGraphResponse finalTree = argumentService.getGraph(publishedAssertion.getStableId(), kyle.getUser().getNodeId());
        Assert.assertEquals(assertionNode.getId(), finalTree.getRootId());

        // Make sure the drafts are all gone
        Assert.assertNull(argumentService.getFullDetail(assertionDraft.getStableId()));
        Assert.assertNull(argumentService.getFullDetail(interpDraft.getStableId()));
        Assert.assertNull(argumentService.getFullDetail(edited.getStableId()));
    }

    @Test
    public void testChildDraftCreation() throws NotAuthorizedException, NodeRulesException {
        Author kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = createPublishedAssertion();

        InterpretationNode interp = (InterpretationNode) assertionNode.getGraphChildren().iterator().next();
        SourceNode sourceNode = interp.getSource();

        EditResult result = argumentService.makeDraft(kyle.getUser().getNodeId(), kyle.getStableId(), sourceNode.getId());

        Assert.assertEquals(sourceNode.getId(), result.getEditedNode().getPreviousVersion().getId()); // The draft node shows the original as its previous version.
        Assert.assertEquals(result.getEditedNode().getId(), result.getGraph().getRootId()); // The root was the subject of editing
        Assert.assertFalse(result.getEditedNode().getBody().isPublic());
    }

    @Test
    public void testPublishWithSessionClears() throws NodeRulesException, NotAuthorizedException {

        Author jim = registerUser("1234", "Jim");
        List<Long> links = new LinkedList<>();

        AssertionNode assertionNode = argumentService.createAssertion(jim.getUser().getNodeId(), jim.getStableId(), "Assertion Title", "Original",
                "Hello, world!", links);
        session.clear();

        InterpretationNode interpretationNode = argumentService.createInterpretation(jim.getUser().getNodeId(), jim.getStableId(), "Interp Title",
                "Original", "Interp body", null);
        session.clear();

        // Edit the assertion to point to the interpretation
        links.add(interpretationNode.getId());
        assertionNode = argumentService.editAssertion(jim.getUser().getNodeId(), assertionNode.getId(), "Assertion Title", "Q", "Hello! {{[" +
                interpretationNode.getBody().getMajorVersion().getStableId() + "]link}}", links);
        session.clear();

        SourceNode sourceNode = argumentService.createSource(jim.getUser().getNodeId(), jim.getStableId(), "Source Title", "Original", "http://google.com");
        session.clear();

        argumentService.editInterpretation(jim.getUser().getNodeId(), interpretationNode.getId(), "Interp Title", "Q2", "Interp body", sourceNode.getId());
        session.clear();

        AssertionNode published = (AssertionNode) argumentService.publishNode(jim.getUser().getNodeId(), assertionNode.getId()).getRootNode();

        Assert.assertTrue(published.getBody().isPublic());
    }


    @Test
    public void testPublishingChildOfDraft() throws NotAuthorizedException, NodeRulesException {
        Author kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = createPublishedAssertion();

        EditResult rootDraft = argumentService.makeDraft(kyle.getUser().getNodeId(), kyle.getStableId(), assertionNode.getId());

        ArgumentNode childOriginal = assertionNode.getGraphChildren().iterator().next();
        Assert.assertEquals(childOriginal.getId(), rootDraft.getEditedNode().getGraphChildren().iterator().next().getId());

        session.clear();

        ArgumentNode childDraft = argumentService.makeDraft(kyle.getUser().getNodeId(), kyle.getStableId(), childOriginal.getId()).getEditedNode();

        // The javascript will call edit on the parent (which is a draft) to make it point to this draft version of the child.
        List<Long> links = new LinkedList<>();
        links.add(childDraft.getId());
        String body = "New Body {{[" + childDraft.getBody().getMajorVersion().getStableId() + "]link}}";
        argumentService.editAssertion(kyle.getUser().getNodeId(), rootDraft.getEditedNode().getId(), "Ed Root", "Qual", body, links);

        session.clear();

        // Now publish the child
        ArgumentNode publishedChild = argumentService.publishNode(kyle.getUser().getNodeId(), childDraft.getId()).getRootNode();

        session.clear();

        // Make sure the published child has same id as original child
        Assert.assertEquals(childOriginal.getStableId(), publishedChild.getStableId());

        // The draft root should now be pointing to the published version of the child.
        ArgumentNode draftNow = argumentService.getGraph(rootDraft.getEditedNode().getStableId(), kyle.getUser().getNodeId()).getRootNode();
        Assert.assertEquals(1, draftNow.getGraphChildren().size());
        Assert.assertEquals(publishedChild.getStableId(), draftNow.getGraphChildren().iterator().next().getStableId());

        // The original root should also be pointing to the published version of the child.
        ArgumentNode rootNow = argumentService.getGraph(assertionNode.getStableId(), kyle.getUser().getNodeId()).getRootNode();
        Assert.assertEquals(1, rootNow.getGraphChildren().size());
        Assert.assertEquals(publishedChild.getStableId(), rootNow.getGraphChildren().iterator().next().getStableId());

        // The published child should have two consumers
        Set<ArgumentNode> consumers = argumentService.getGraph(publishedChild.getStableId(), kyle.getUser().getNodeId()).getConsumers();
        Assert.assertEquals(2, consumers.size());
    }

    @Test
    public void testPublishingDraftWithDraftChild() throws NotAuthorizedException, NodeRulesException {
        Author kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = createPublishedAssertion();

        EditResult rootDraft = argumentService.makeDraft(kyle.getUser().getNodeId(), kyle.getStableId(), assertionNode.getId());

        ArgumentNode childOriginal = assertionNode.getGraphChildren().iterator().next();
        Assert.assertEquals(childOriginal.getId(), rootDraft.getEditedNode().getGraphChildren().iterator().next().getId());

        session.clear();

        ArgumentNode childDraft = argumentService.makeDraft(kyle.getUser().getNodeId(), kyle.getStableId(), childOriginal.getId()).getEditedNode();

        // The javascript will call edit on the parent (which is a draft) to make it point to this draft version of the child.
        List<Long> links = new LinkedList<>();
        links.add(childDraft.getId());
        String body = "New Body {{[" + childDraft.getBody().getMajorVersion().getStableId() + "]link}}";
        argumentService.editAssertion(kyle.getUser().getNodeId(), rootDraft.getEditedNode().getId(), "Ed Root", "Qual", body, links);

        session.clear();

        // Now publish the parent. This used to throw a null pointer.
        ArgumentNode publishedNode = argumentService.publishNode(kyle.getUser().getNodeId(), rootDraft.getEditedNode().getId()).getRootNode();

        Assert.assertEquals(publishedNode.getStableId(), assertionNode.getStableId());

    }

    @Test
    public void testRegularConsumers() throws NotAuthorizedException, NodeRulesException {
        Author kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = ArgumentTestUtil.createPublishedTriple(argumentService, kyle);
        ArgumentNode childOriginal = assertionNode.getGraphChildren().iterator().next();

        session.clear();

        Set<ArgumentNode> consumers = argumentService.getGraph(childOriginal.getStableId(), kyle.getUser().getNodeId()).getConsumers();
        Assert.assertEquals(1, consumers.size());
    }

    @Test
    public void testConsumersWhichAreDrafts() throws NotAuthorizedException, NodeRulesException {
        Author kyle = registerUser("5678", "Kyle");
        AssertionNode assertionNode = ArgumentTestUtil.createPublishedTriple(argumentService, kyle);

        EditResult rootDraft = argumentService.makeDraft(kyle.getUser().getNodeId(), kyle.getStableId(), assertionNode.getId());

        ArgumentNode childOriginal = assertionNode.getGraphChildren().iterator().next();
        Assert.assertEquals(childOriginal.getId(), rootDraft.getEditedNode().getGraphChildren().iterator().next().getId());

        session.clear();

        Set<ArgumentNode> consumers = argumentService.getGraph(childOriginal.getStableId(), kyle.getUser().getNodeId()).getConsumers();
        Assert.assertEquals(2, consumers.size());
    }


    private AssertionNode createPublishedAssertion() throws NodeRulesException, NotAuthorizedException {

        Author jim = registerUser("1234", "Jim");
        return ArgumentTestUtil.createPublishedTriple(argumentService, jim);
    }


}
