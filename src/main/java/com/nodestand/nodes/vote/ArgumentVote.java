package com.nodestand.nodes.vote;

import com.nodestand.nodes.ArgumentBody;
import com.nodestand.nodes.User;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="ARGUMENT_VOTE")
public class ArgumentVote {
    @GraphId
    public Long id;

    public VoteType voteType;

    @StartNode
    public User user;

    @EndNode
    public ArgumentBody argumentBody;

}
