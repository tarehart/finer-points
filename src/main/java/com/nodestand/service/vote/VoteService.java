package com.nodestand.service.vote;

import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.User;
import com.nodestand.nodes.vote.VoteType;

/**
 * Created by Tyler on 1/1/2017.
 */
public interface VoteService {

    void voteNode(String userStableId, String nodeStableId, VoteType voteType) throws NodeRulesException;

    void unvoteNode(String nodeStableId, String userStableId) throws NodeRulesException;
}
