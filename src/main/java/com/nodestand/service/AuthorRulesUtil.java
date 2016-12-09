package com.nodestand.service;

import com.nodestand.nodes.Author;
import com.nodestand.nodes.NodeRulesException;
import com.nodestand.nodes.repository.UserRepository;

public final class AuthorRulesUtil {

    public static Author loadAuthorWithSecurityCheck(UserRepository userRepo, long userId, String authorStableId) throws NodeRulesException {
        Author author = userRepo.loadAuthorWithUser(authorStableId);

        if (!author.getUser().getNodeId().equals(userId)) {
            throw new NodeRulesException("User " + userId + " is not allowed to control the alias " + author.getDisplayName());
        }
        return author;
    }

}
