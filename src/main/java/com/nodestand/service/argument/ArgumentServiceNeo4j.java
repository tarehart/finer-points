package com.nodestand.service.argument;

import com.nodestand.controllers.serial.EditResult;
import com.nodestand.controllers.serial.QuickEdge;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationBody;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.source.SourceBody;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.service.VersionHelper;
import com.nodestand.util.TwoWayUtil;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Component
public class ArgumentServiceNeo4j implements ArgumentService {

    @Autowired
    Session session;

    @Autowired
    ArgumentNodeRepository argumentRepo;

    @Autowired
    VersionHelper versionHelper;


    @Override
    @Transactional
    public QuickGraphResponse getGraph(String rootStableId) {
        Set<ArgumentNode> nodes = argumentRepo.getGraph(rootStableId);

        Set<QuickEdge> edges = new HashSet<>();

        Long rootId = null;

        for (ArgumentNode n: nodes) {
            for (ArgumentNode child : n.getGraphChildren()) {
                edges.add(new QuickEdge(n.getId(), child.getId()));
            }
            if (n.getStableId().equals(rootStableId)) {
                rootId = n.getId();
            }
        }

        return new QuickGraphResponse(nodes, edges, rootId, rootStableId);
    }

    @Override
    @Transactional
    public ArgumentNode getFullDetail(long nodeId) {
        ArgumentNode node = session.load(ArgumentNode.class, nodeId);
        session.load(ArgumentBody.class, node.getBody().getId(), 2);
        return node;
    }

    @Override
    @Transactional
    public AssertionNode createAssertion(long userId, String title, String body, Collection<Long> links) {

        User user = session.load(User.class, userId);
        AssertionBody assertionBody = new AssertionBody(title, body, user);

        AssertionNode node = assertionBody.constructNode(versionHelper);

        for (Long id : links) {
            ArgumentNode linked = session.load(ArgumentNode.class, id);
            node.supportedBy(linked);
        }

        session.save(node);
        return node;
    }

    @Override
    @Transactional
    public InterpretationNode createInterpretation(long userId, String title, String body, Long sourceId) {

        User user = session.load(User.class, userId);
        InterpretationBody interpretationBody = new InterpretationBody(title, body, user);

        InterpretationNode node = interpretationBody.constructNode(versionHelper);

        if (sourceId != null) {
            SourceNode source = session.load(SourceNode.class, sourceId);
            node.setSource(source);
        }

        session.save(node);
        return node;
    }

    @Override
    @Transactional
    public SourceNode createSource(long userId, String title, String url) {
        User user = session.load(User.class, userId);

        SourceBody sourceBody = new SourceBody(title, user, url);
        SourceNode node = sourceBody.constructNode(versionHelper);

        session.save(node);
        return node;
    }

    @Override
    @Transactional
    public AssertionNode editAssertion(long userId, long nodeId, String title, String body, Collection<Long> links) throws NodeRulesException {
        User user = session.load(User.class, userId);
        AssertionNode existingNode = session.load(AssertionNode.class, nodeId, 2);

        checkEditRules(existingNode);

        existingNode.getBody().setTitle(title);
        existingNode.getBody().setBody(body);

        Set<ArgumentNode> children = new HashSet<>();
        for (Long id : links) {
            ArgumentNode supportingNode = session.load(ArgumentNode.class, id);
            if (supportingNode instanceof SourceNode) {
                throw new NodeRulesException("An assertion node cannot link directly to a source!");
            }
            children.add(supportingNode);
        }

        TwoWayUtil.updateSupportingNodes(existingNode, children);

        session.save(existingNode);
        return existingNode;
    }



    @Override
    @Transactional
    public InterpretationNode editInterpretation(long userId, long nodeId, String title, String body, Long sourceId) throws NodeRulesException {
        User user = session.load(User.class, userId);

        InterpretationNode existingNode = session.load(InterpretationNode.class, nodeId, 2);

        checkEditRules(existingNode);

        existingNode.getBody().setTitle(title);
        existingNode.getBody().setBody(body);

        SourceNode sourceNode = session.load(SourceNode.class, sourceId);

        existingNode.setSource(sourceNode);

        session.save(existingNode);
        return existingNode;
    }

    @Override
    @Transactional
    public SourceNode editSource(long userId, long nodeId, String title, String url) throws NodeRulesException {
        User user = session.load(User.class, userId);

        SourceNode existingNode = session.load(SourceNode.class, nodeId, 2);

        checkEditRules(existingNode);

        existingNode.getBody().setTitle(title);
        existingNode.getBody().setUrl(url);

        session.save(existingNode);
        return existingNode;
    }

    @Override
    @Transactional
    public EditResult makeDraft(long userId, long nodeId, String rootStableId) throws NodeRulesException {

        ArgumentNode existingNode = session.load(ArgumentNode.class, nodeId, 2);

        if (!existingNode.getBody().isEditable()) {
            throw new NodeRulesException("Cannot edit this node!");
        }

        if (!existingNode.getBody().isPublic()) {
            // Already a draft.
            throw new NodeRulesException("This is already a draft, should not attempt to split off a new draft");
        }

        // Create a new draft version

        User user = session.load(User.class, userId);

        // This will set the previous version on the draft. Later, when we publish the edit,
        // this draft will copy its contents to the previous version and then be destroyed.
        ArgumentNode draftNode = existingNode.createNewDraft(VersionHelper.startBuild(user), true);

        session.save(draftNode);

        String newRootStableId = null;
        if (existingNode.getStableId().equals(rootStableId)) {
            newRootStableId = draftNode.getStableId();
        } else {
            newRootStableId = DraftPropagation.propagateDraftTowardRoot(draftNode, getGraph(rootStableId), session, argumentRepo);
        }

        EditResult result = new EditResult(draftNode);
        result.setGraph(getGraph(newRootStableId));

        return result;
    }

    @Override
    @Transactional
    public Set<ArgumentNode> getNodesInMajorVersion(long majorVersionId) {
        return argumentRepo.getNodesInMajorVersion(majorVersionId);
    }

    private void checkEditRules(ArgumentNode existingNode) throws NodeRulesException {
        if (!existingNode.getBody().isEditable()) {
            throw new NodeRulesException("Cannot edit this node!");
        }

        if (existingNode.getBody().isPublic()) {
            throw new NodeRulesException("Must split off a private draft before editing.");
        }
    }


}
