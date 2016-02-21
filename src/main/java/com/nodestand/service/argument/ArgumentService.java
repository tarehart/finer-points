package com.nodestand.service.argument;

import com.nodestand.controllers.serial.EditResult;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;

import java.util.Collection;
import java.util.Set;

public interface ArgumentService {

    QuickGraphResponse getGraph(String rootStableId);

    ArgumentNode getFullDetail(long nodeId);

    ArgumentNode createAssertion(long userId, String title, String body, Collection<Long> links);

    ArgumentNode createInterpretation(long userId, String title, String body, Long sourceId);

    ArgumentNode createSource(long userId, String title, String url);

    ArgumentNode editAssertion(long userId, long nodeId, String title, String body, Collection<Long> links) throws NodeRulesException;

    ArgumentNode editInterpretation(long userId, long nodeId, String title, String body, Long sourceId) throws NodeRulesException;

    ArgumentNode editSource(long userId, long nodeId, String title, String url) throws NodeRulesException;

    EditResult makeDraft(long userId, long nodeId, String rootStableId) throws NodeRulesException;

    Set<ArgumentNode> getNodesInMajorVersion(long majorVersionId);

}
