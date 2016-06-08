package com.nodestand.service.argument;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.controllers.serial.EditResult;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.source.SourceNode;

import java.util.Collection;
import java.util.Set;

public interface ArgumentService {

    QuickGraphResponse getGraph(String rootStableId);

    ArgumentNode getFullDetail(long nodeId);

    AssertionNode createAssertion(long userId, String title, String qualifier, String body, Collection<Long> links) throws NodeRulesException;

    InterpretationNode createInterpretation(long userId, String title, String qualifier, String body, Long sourceId);

    SourceNode createSource(long userId, String title, String qualifier, String url);

    AssertionNode editAssertion(long userId, long nodeId, String title, String qualifier, String body, Collection<Long> links) throws NodeRulesException;

    InterpretationNode editInterpretation(long userId, long nodeId, String title, String qualifier, String body, Long sourceId) throws NodeRulesException;

    SourceNode editSource(long userId, long nodeId, String title, String qualifier, String url) throws NodeRulesException;

    EditResult makeDraft(long userId, long nodeId) throws NodeRulesException;

    ArgumentNode publishNode(long userId, long nodeId) throws NotAuthorizedException, NodeRulesException;

    Set<ArgumentNode> getNodesInMajorVersion(long majorVersionId);

    Set<ArgumentNode> getRootNodes();

    Set<ArgumentNode> getDraftNodes(long userId);

    Set<ArgumentNode> getConsumerNodes(long nodeId);

    Set<ArgumentNode> getConsumerNodesIncludingDrafts(long userId, long nodeId);

}
