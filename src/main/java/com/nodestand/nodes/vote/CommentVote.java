package com.nodestand.nodes.vote;

import com.nodestand.nodes.User;
import com.nodestand.nodes.comment.Comment;
import org.neo4j.ogm.annotation.EndNode;
import org.neo4j.ogm.annotation.GraphId;
import org.neo4j.ogm.annotation.RelationshipEntity;
import org.neo4j.ogm.annotation.StartNode;

@RelationshipEntity(type="COMMENT_VOTE")
public class CommentVote {
    @GraphId
    public Long id;

    public boolean isUpvote;

    @StartNode
    public User user;

    @EndNode
    public Comment comment;

}
