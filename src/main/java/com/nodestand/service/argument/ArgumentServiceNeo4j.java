package com.nodestand.service.argument;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.controllers.ResourceNotFoundException;
import com.nodestand.controllers.serial.EditResult;
import com.nodestand.controllers.serial.QuickEdge;
import com.nodestand.controllers.serial.QuickGraphResponse;
import com.nodestand.nodes.*;
import com.nodestand.nodes.assertion.AssertionBody;
import com.nodestand.nodes.assertion.AssertionNode;
import com.nodestand.nodes.interpretation.InterpretationBody;
import com.nodestand.nodes.interpretation.InterpretationNode;
import com.nodestand.nodes.repository.ArgumentNodeRepository;
import com.nodestand.nodes.repository.UserRepository;
import com.nodestand.nodes.source.SourceBody;
import com.nodestand.nodes.source.SourceNode;
import com.nodestand.nodes.subject.SubjectBody;
import com.nodestand.nodes.subject.SubjectNode;
import com.nodestand.service.AuthorRulesUtil;
import com.nodestand.service.VersionHelper;
import com.nodestand.util.TwoWayUtil;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ArgumentServiceNeo4j implements ArgumentService {

    private final ArgumentNodeRepository argumentRepo;

    private final UserRepository userRepo;

    private final VersionHelper versionHelper;

    private final Session session;

    @Autowired
    public ArgumentServiceNeo4j(ArgumentNodeRepository argumentRepo, UserRepository userRepo, VersionHelper versionHelper, Session session) {
        this.argumentRepo = argumentRepo;
        this.userRepo = userRepo;
        this.versionHelper = versionHelper;
        this.session = session;
    }

    @Override
    @Transactional
    public QuickGraphResponse getGraph(String rootStableId, Long userId) {
        Set<ArgumentNode> nodes = argumentRepo.getGraph(rootStableId);

        Set<ArgumentNode> consumers;
        if (userId != null) {
            consumers = argumentRepo.getConsumerNodes(rootStableId, userId);
        } else {
            consumers = argumentRepo.getConsumerNodes(rootStableId);
        }

        // This should enhance the node already in our set.
        argumentRepo.getNodeRich(rootStableId);

        if (nodes.isEmpty()) {
            throw new ResourceNotFoundException("Node not found!");
        }

        Set<QuickEdge> edges = new HashSet<>();

        Long rootId = null;

        for (ArgumentNode n: nodes) {
            for (Node child : n.getGraphChildren()) {
                edges.add(new QuickEdge(n.getId(), child.getId()));
            }
            if (n.getStableId().equals(rootStableId)) {
                rootId = n.getId();
            }
        }

        return new QuickGraphResponse(nodes, edges, rootId, rootStableId, consumers);
    }

    @Override
    @Transactional
    public ArgumentNode getFullDetail(String stableId) {
        return argumentRepo.getNodeRich(stableId);
    }

    @Override
    @Transactional
    public AssertionNode createAssertion(long userId, String authorStableId, String title, String qualifier, String body, Collection<Long> links) throws NodeRulesException {

        Author author = AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, authorStableId);

        AssertionBody assertionBody = new AssertionBody(title, qualifier, body, author);

        AssertionNode node = assertionBody.constructNode();

        Set<Node> children = getAndValidateChildNodes(links);

        TwoWayUtil.updateSupportingNodes(node, children);

        node.updateChildOrder(argumentRepo);

        List<String> mvIds = children.stream()
                .map(c -> c.getBody().getMajorVersion().getStableId())
                .collect(Collectors.toList());

        assertionBody.getMajorVersion().mergeEdgeOwners(author, mvIds);

        session.save(node);
        return node;
    }



    @Override
    @Transactional
    public InterpretationNode createInterpretation(long userId, String authorStableId, String title, String qualifier, String body, Long leafId) throws NodeRulesException {

        Author author = AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, authorStableId);
        InterpretationBody interpretationBody = new InterpretationBody(title, qualifier, body, author);

        InterpretationNode node = interpretationBody.constructNode();

        if (leafId != null) {

            LeafNode leafNode = (LeafNode) argumentRepo.loadWithMajorVersion(leafId);
            node.setLeafNode(leafNode);
            interpretationBody.getMajorVersion().mergeEdgeOwner(author, leafNode.getBody().getMajorVersion().getStableId());
        }

        session.save(node);
        return node;
    }

    @Override
    @Transactional
    public SourceNode createSource(long userId, String authorStableId, String title, String qualifier, String url) throws NodeRulesException {

        Author author = AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, authorStableId);

        SourceBody sourceBody = new SourceBody(title, qualifier, author, url);
        SourceNode node = sourceBody.constructNode();

        session.save(node);
        return node;
    }

    @Override
    @Transactional
    public SubjectNode createSubject(long userId, String authorStableId, String title, String qualifier, String url) throws NodeRulesException {

        Author author = AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, authorStableId);

        SubjectBody subjectBody = new SubjectBody(title, qualifier, author, url);
        SubjectNode node = subjectBody.constructNode();

        session.save(node);
        return node;
    }

    @Override
    @Transactional
    public AssertionNode editAssertion(long userId, long nodeId, String title, String qualifier, String body, Collection<Long> links) throws NodeRulesException {

        AssertionNode existingNode = (AssertionNode) loadWithChildren(nodeId);

        Author author = AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, existingNode.getBody().author.getStableId());

        checkEditRules(existingNode);

        existingNode.getBody().setTitle(title);
        existingNode.getBody().setQualifier(qualifier);
        existingNode.getBody().setBody(body);

        Set<Node> children = getAndValidateChildNodes(links);

        TwoWayUtil.updateSupportingNodes(existingNode, children);

        existingNode.updateChildOrder(argumentRepo);

        List<String> mvIds = children.stream()
                .map(c -> c.getBody().getMajorVersion().getStableId())
                .collect(Collectors.toList());

        existingNode.getBody().getMajorVersion().mergeEdgeOwners(author, mvIds);

        session.save(existingNode);
        return existingNode;
    }

    private ArgumentNode loadWithChildren(long nodeId) throws NodeRulesException {
        Set<ArgumentNode> argumentNodes = argumentRepo.loadWithMajorVersionAndChildren(nodeId);
        List<ArgumentNode> matches = argumentNodes.stream().filter(n -> n.getId().equals(nodeId)).collect(Collectors.toList());
        if (matches.size() != 1) {
            throw new NodeRulesException("Expected query to return exactly one node with id " + nodeId);
        }
        return matches.get(0);
    }

    private Set<Node> getAndValidateChildNodes(Collection<Long> links) throws NodeRulesException {
        Set<Node> children = new HashSet<>();
        for (Long id : links) {
            ArgumentNode supportingNode = argumentRepo.loadWithMajorVersion(id);
            if (supportingNode instanceof LeafNode) {
                throw new NodeRulesException("An assertion node cannot link directly to a leaf node!");
            }
            children.add(supportingNode);
        }
        return children;
    }


    @Override
    @Transactional
    public InterpretationNode editInterpretation(long userId, long nodeId, String title, String qualifier, String body, Long sourceId) throws NodeRulesException {

        InterpretationNode existingNode = (InterpretationNode) loadWithChildren(nodeId);

        Author author = AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, existingNode.getBody().author.getStableId());

        checkEditRules(existingNode);

        existingNode.getBody().setTitle(title);
        existingNode.getBody().setQualifier(qualifier);
        existingNode.getBody().setBody(body);

        LeafNode leafNode = null;
        if (sourceId != null) {
            leafNode = (LeafNode) argumentRepo.loadWithMajorVersion(sourceId);
            existingNode.getBody().getMajorVersion().mergeEdgeOwner(author, leafNode.getBody().getMajorVersion().getStableId());

        }

        TwoWayUtil.updateSupportingNodes(existingNode, leafNode);

        session.save(existingNode);
        return existingNode;
    }

    @Override
    @Transactional
    public SourceNode editSource(long userId, long nodeId, String title, String qualifier, String url) throws NodeRulesException {

        SourceNode existingNode = (SourceNode) argumentRepo.loadWithMajorVersion(nodeId);

        AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, existingNode.getBody().author.getStableId());

        checkEditRules(existingNode);

        existingNode.getBody().setTitle(title);
        existingNode.getBody().setQualifier(qualifier);
        existingNode.getBody().setUrl(url);

        session.save(existingNode);
        return existingNode;
    }

    @Override
    @Transactional
    public SubjectNode editSubject(long userId, long nodeId, String title, String qualifier, String url) throws NodeRulesException {

        SubjectNode existingNode = (SubjectNode) argumentRepo.loadWithMajorVersion(nodeId);

        AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, existingNode.getBody().author.getStableId());

        checkEditRules(existingNode);

        existingNode.getBody().setTitle(title);
        existingNode.getBody().setQualifier(qualifier);
        existingNode.getBody().setUrl(url);

        session.save(existingNode);
        return existingNode;
    }

    @Override
    @Transactional
    public EditResult makeDraft(long userId, String authorStableId, long nodeId) throws NodeRulesException {

        Author author = AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, authorStableId);

        ArgumentNode existingNode = session.load(ArgumentNode.class, nodeId, 2);

        if (!existingNode.getBody().isEditable()) {
            throw new NodeRulesException("Cannot edit this node!");
        }

        if (!existingNode.getBody().isPublic()) {
            // Already a draft.
            throw new NodeRulesException("This is already a draft, should not attempt to split off a new draft");
        }

        // Create a new draft version

        // This will set the previous version on the draft. Later, when we publish the edit,
        // this draft will copy its contents to the previous version and then be destroyed.
        ArgumentNode draftNode = existingNode.createNewDraft(author);

        session.save(draftNode);

        EditResult result = new EditResult(draftNode);
        result.setGraph(getGraph(draftNode.getStableId(), userId));

        return result;
    }

    @Override
    @Transactional
    public QuickGraphResponse publishNode(long userId, long nodeId) throws NotAuthorizedException, NodeRulesException {

        ArgumentNode existingNode = session.load(ArgumentNode.class, nodeId, 2);

        if (existingNode == null) {
            throw new ResourceNotFoundException("Could not find node with id " + nodeId);
        }

        AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, existingNode.getBody().author.getStableId());

        if (existingNode.isFinalized()) {
            throw new NodeRulesException("No new changes to publish!");
        }

        Node resultingNode = versionHelper.publish(existingNode);

        return getGraph(resultingNode.getStableId(), userId);
    }

    @Override
    @Transactional
    public Set<ArgumentNode> getNodesInMajorVersion(long majorVersionId) {
        return argumentRepo.getNodesInMajorVersion(majorVersionId);
    }

    @Override
    public Set<ArgumentNode> getRootNodes() {
        return argumentRepo.getRootNodesRich();
    }

    @Override
    public Set<ArgumentNode> getDraftNodes(long userId, String authorStableId) throws NodeRulesException {
        AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, authorStableId);
        return argumentRepo.getDraftNodesRich(authorStableId);
    }

    @Override
    public Set<ArgumentNode> getNodesPublishedByAuthor(String authorStableId) {
        return argumentRepo.getNodesOriginallyAuthoredByUser(authorStableId);
    }

    @Override
    public ArgumentNode getEditHistory(String stableId) {
        Set<ArgumentNode> editHistory = argumentRepo.getEditHistory(stableId);
        List<ArgumentNode> matches = editHistory.stream().filter(n -> n.getStableId().equals(stableId)).collect(Collectors.toList());
        assert matches.size() == 1;
        return matches.get(0);
    }

    @Override
    public void discardDraft(Long userId, String stableId) throws NodeRulesException {

        ArgumentNode draftNode = argumentRepo.getNodeRich(stableId);

        if (draftNode.getBody().isPublic()) {
            throw new NodeRulesException("Can't discard this node, it's not a draft!");
        }

        AuthorRulesUtil.loadAuthorWithSecurityCheck(userRepo, userId, draftNode.getBody().author.getStableId());

        session.delete(draftNode);
        session.delete(draftNode.getBody());
        TwoWayUtil.forgetNode(draftNode);
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
