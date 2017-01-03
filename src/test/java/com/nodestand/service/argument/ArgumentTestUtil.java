package com.nodestand.service.argument;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.nodes.source.SourceNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Tyler on 2/29/2016.
 */
public class ArgumentTestUtil {

    public static AssertionNode createPublishedTriple(ArgumentService argumentService, Author jim) throws NodeRulesException, NotAuthorizedException {
        List<Long> links = new LinkedList<>();

        AssertionNode assertionNode = argumentService.createAssertion(jim.getUser().getNodeId(), jim.getStableId(), "Assertion Title", "Original", "Hello, world!", links);

        InterpretationNode interpretationNode = argumentService.createInterpretation(jim.getUser().getNodeId(), jim.getStableId(), "Interp Title", "Orig int", "Interp body", null);

        // Edit the assertion to point to the interpretation
        links.add(interpretationNode.getId());
        assertionNode = argumentService.editAssertion(jim.getUser().getNodeId(), assertionNode.getId(), "Assertion Title", "QA", "Hello! {{[" +
                interpretationNode.getBody().getMajorVersion().getStableId() + "]link}}", links);

        SourceNode sourceNode = argumentService.createSource(jim.getUser().getNodeId(), jim.getStableId(), "Source Title", "Original src", "http://google.com");

        argumentService.editInterpretation(jim.getUser().getNodeId(), interpretationNode.getId(), "Interp Title", "QI", "Interp body", sourceNode.getId());


        return (AssertionNode) argumentService.publishNode(jim.getUser().getNodeId(), assertionNode.getId()).getRootNode();
    }

    public static AssertionNode createPublishedTreeSmall(ArgumentService argumentService, Author jim) throws NotAuthorizedException, NodeRulesException {

        AssertionNode triple = createPublishedTriple(argumentService, jim);

        List<Long> links = new LinkedList<>();

        AssertionNode root = argumentService.createAssertion(jim.getUser().getNodeId(), jim.getStableId(), "Root title", "Orig", "Hi root", links);

        InterpretationNode interpretationNode = argumentService.createInterpretation(jim.getUser().getNodeId(), jim.getStableId(), "Interp Title", "Qual", "Interp body", null);

        // Edit the assertion to point to the interpretation and the triple
        links.add(interpretationNode.getId());
        links.add(triple.getId());
        root = argumentService.editAssertion(jim.getUser().getNodeId(), root.getId(), "Root Title", "Orig", "Hello! {{[" +
                interpretationNode.getBody().getMajorVersion().getStableId() + "]link}} and {{[" + triple.getBody().getMajorVersion().getStableId() + "]link2}}", links);

        SourceNode sourceNode = argumentService.createSource(jim.getUser().getNodeId(), jim.getStableId(), "ForkSource Title", "Forked", "http://google.com");

        argumentService.editInterpretation(jim.getUser().getNodeId(), interpretationNode.getId(), "ForkInterp Title", "QF", "ForkInterp body", sourceNode.getId());


        return (AssertionNode) argumentService.publishNode(jim.getUser().getNodeId(), root.getId()).getRootNode();
    }

    public static NodeAndRoot createPublishedMultiPathSmall(ArgumentService argumentService, Author jim) throws NotAuthorizedException, NodeRulesException {

        AssertionNode triple = createPublishedTriple(argumentService, jim);

        List<Long> links = new LinkedList<>();

        AssertionNode root = argumentService.createAssertion(jim.getUser().getNodeId(), jim.getStableId(), "Root title", "Original", "Hi root", links);

        InterpretationNode interpretationNode = (InterpretationNode) triple.getGraphChildren().stream().findFirst().get();

        // Edit the assertion to point to the interpretation and the triple
        links.add(interpretationNode.getId());
        links.add(triple.getId());
        root = argumentService.editAssertion(jim.getUser().getNodeId(), root.getId(), "Root Title", "QR", "Hello! {{[" + interpretationNode.getId() + "]link}} and {{[" + triple.getId() + "]link2}}", links);

        root = (AssertionNode) argumentService.publishNode(jim.getUser().getNodeId(), root.getId()).getRootNode();

        return new NodeAndRoot(interpretationNode, root);
    }

    public static Author registerUser(UserRepository userRepository, String socialId, String name) {

        final User user = new User(
                "google",
                socialId,
                User.Roles.ROLE_USER);

        Author author = user.addNewAlias(name);

        userRepository.save(user);

        return author;
    }


    public static class NodeAndRoot {
        public final ArgumentNode root;
        public final ArgumentNode node;

        public NodeAndRoot(ArgumentNode node, ArgumentNode root) {
            this.root = root;
            this.node = node;
        }
    }


}
