package com.nodestand.service.argument;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.source.SourceNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Tyler on 2/29/2016.
 */
public class ArgumentTestUtil {

    public static AssertionNode createPublishedTriple(ArgumentService argumentService, User jim) throws NodeRulesException, NotAuthorizedException {
        List<Long> links = new LinkedList<>();

        AssertionNode assertionNode = argumentService.createAssertion(jim.getNodeId(), "Assertion Title", "Hello, world!", links);

        InterpretationNode interpretationNode = argumentService.createInterpretation(jim.getNodeId(), "Interp Title", "Interp body", null);

        // Edit the assertion to point to the interpretation
        links.add(interpretationNode.getId());
        assertionNode = argumentService.editAssertion(jim.getNodeId(), assertionNode.getId(), "Assertion Title", "Hello! {{[" + interpretationNode.getId() + "]link}}", links);

        SourceNode sourceNode = argumentService.createSource(jim.getNodeId(), "Source Title", "http://google.com");

        argumentService.editInterpretation(jim.getNodeId(), interpretationNode.getId(), "Interp Title", "Interp body", sourceNode.getId());


        return (AssertionNode) argumentService.publishNode(jim.getNodeId(), assertionNode.getId());
    }

    public static AssertionNode createPublishedTreeSmall(ArgumentService argumentService, User jim) throws NotAuthorizedException, NodeRulesException {

        AssertionNode triple = createPublishedTriple(argumentService, jim);

        List<Long> links = new LinkedList<>();

        AssertionNode root = argumentService.createAssertion(jim.getNodeId(), "Root title", "Hi root", links);

        InterpretationNode interpretationNode = argumentService.createInterpretation(jim.getNodeId(), "Interp Title", "Interp body", null);

        // Edit the assertion to point to the interpretation and the triple
        links.add(interpretationNode.getId());
        links.add(triple.getId());
        root = argumentService.editAssertion(jim.getNodeId(), root.getId(), "Root Title", "Hello! {{[" + interpretationNode.getId() + "]link}} and {{[" + triple.getId() + "]link2}}", links);

        SourceNode sourceNode = argumentService.createSource(jim.getNodeId(), "ForkSource Title", "http://google.com");

        argumentService.editInterpretation(jim.getNodeId(), interpretationNode.getId(), "ForkInterp Title", "ForkInterp body", sourceNode.getId());


        return (AssertionNode) argumentService.publishNode(jim.getNodeId(), root.getId());
    }

    public static NodeAndRoot createPublishedMultiPathSmall(ArgumentService argumentService, User jim) throws NotAuthorizedException, NodeRulesException {

        AssertionNode triple = createPublishedTriple(argumentService, jim);

        List<Long> links = new LinkedList<>();

        AssertionNode root = argumentService.createAssertion(jim.getNodeId(), "Root title", "Hi root", links);

        InterpretationNode interpretationNode = (InterpretationNode) triple.getGraphChildren().stream().findFirst().get();

        // Edit the assertion to point to the interpretation and the triple
        links.add(interpretationNode.getId());
        links.add(triple.getId());
        root = argumentService.editAssertion(jim.getNodeId(), root.getId(), "Root Title", "Hello! {{[" + interpretationNode.getId() + "]link}} and {{[" + triple.getId() + "]link2}}", links);

        root = (AssertionNode) argumentService.publishNode(jim.getNodeId(), root.getId());

        return new NodeAndRoot(interpretationNode, root);
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
