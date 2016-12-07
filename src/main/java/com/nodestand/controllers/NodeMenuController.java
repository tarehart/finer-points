package com.nodestand.controllers;

import com.nodestand.auth.NotAuthorizedException;
import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.ArgumentNode;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.service.AuthorRulesUtil;
import com.nodestand.service.argument.ArgumentService;
import com.nodestand.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@RestController
public class NodeMenuController {

    private final ArgumentService argumentService;

    private final UserService userService;

    @Autowired
    public NodeMenuController(ArgumentService argumentService, UserService userService) {
        this.argumentService = argumentService;
        this.userService = userService;
    }

    @Transactional
    @RequestMapping("/rootNodes")
    public Set<ArgumentNode> getRootNodes() {

        return argumentService.getRootNodes();
    }

    @Transactional
    @RequestMapping("/draftNodes")
    public Set<ArgumentNode> getDraftNodes(@RequestParam String authorStableId) throws NodeRulesException {

        Long userId = userService.getUserNodeIdFromSecurityContext();
        if (userId == null) {
            throw new NotAuthorizedException("Must be logged in to retrieve drafts.");
        }

        return argumentService.getDraftNodes(userId, authorStableId);
    }

    @Transactional
    @RequestMapping("/nodesPublishedByUser")
    public Set<ArgumentNode> getNodesPublishedByUser(@RequestParam String stableId) {
        return argumentService.getNodesPublishedByAuthor(stableId);
    }

    @Transactional
    @RequestMapping("/consumerNodes")
    public Set<ArgumentNode> getConsumerNodes(@RequestParam Long nodeId) {
        Long userId = userService.getUserNodeIdFromSecurityContext();
        if (userId != null) {
            return argumentService.getConsumerNodesIncludingDrafts(userId, nodeId);
        } else {
            return argumentService.getConsumerNodes(nodeId);
        }
    }

    /**
     * I'm going to the trouble of returning a {@link HistoryResult} because there's not really a great way of
     * returning a simple collection without producing a very inefficient payload. For example, if I returned
     * a Set of ArgumentNode, the json would have a ton of duplication.
     * @param stableId
     * @return
     */
    @Transactional
    @RequestMapping("/nodeEditHistory")
    public HistoryResult getNodeEditHistory(@RequestParam String stableId) {

        ArgumentNode nodeWithHistory = argumentService.getEditHistory(stableId);

        HistoryResult historyResult = new HistoryResult();

        // latestBody is essentially the head of a linked list that we can convert to an array in javascript.
        historyResult.latestBody = nodeWithHistory.getBody();
        historyResult.bodyToStableId = new HashMap<>();

        ArgumentBody current = nodeWithHistory.getBody();
        while (current != null) {
            if (current.getNode() != null) {
                // This will allow us to decorate the history list with links to associated ArgumentNodes
                historyResult.bodyToStableId.put(current.getId(), current.getNode().getStableId());
            }
            current = current.getPreviousVersion();
        }

        return historyResult;
    }

    public class HistoryResult {
        public ArgumentBody latestBody;
        public Map<Long, String> bodyToStableId;
    }

}
