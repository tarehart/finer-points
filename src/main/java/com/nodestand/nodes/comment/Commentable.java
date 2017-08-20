package com.nodestand.nodes.comment;

import com.nodestand.nodes.User;
import org.neo4j.ogm.annotation.NodeEntity;

import java.util.Set;

@NodeEntity
public interface Commentable {

    public Long getId();

    Set<User> getCommentWatchers();
}
