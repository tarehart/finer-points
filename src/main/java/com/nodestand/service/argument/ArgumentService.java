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

    AssertionNode createAssertion(long userId, String title, String body, Collection<Long> links);

    InterpretationNode createInterpretation(long userId, String title, String body, Long sourceId);

    SourceNode createSource(long userId, String title, String url);

    AssertionNode editAssertion(long userId, long nodeId, String title, String body, Collection<Long> links) throws NodeRulesException;

    InterpretationNode editInterpretation(long userId, long nodeId, String title, String body, Long sourceId) throws NodeRulesException;

    SourceNode editSource(long userId, long nodeId, String title, String url) throws NodeRulesException;

    EditResult makeDraft(long userId, long nodeId, String rootStableId) throws NodeRulesException;

    ArgumentNode publishNode(long userId, long nodeId) throws NotAuthorizedException, NodeRulesException;

    Set<ArgumentNode> getNodesInMajorVersion(long majorVersionId);

}
