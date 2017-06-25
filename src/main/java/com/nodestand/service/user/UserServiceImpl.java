package com.nodestand.service.user;

import com.nodestand.auth.NodeUserDetails;
import com.nodestand.controllers.ResourceNotFoundException;
import com.nodestand.nodes.Author;
import com.nodestand.nodes.User;
import com.nodestand.nodes.repository.UserRepository;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private Session session;


    @Override
    public User getUserFromSecurityContext() {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth instanceof UsernamePasswordAuthenticationToken) {
            NodeUserDetails details = (NodeUserDetails) auth.getPrincipal();
            return details.getUser();
        }

        return null;
    }

    @Override
    public Long getUserNodeIdFromSecurityContext() {

        User user = getUserFromSecurityContext();

        if (user != null) {
            return user.getNodeId();
        }

        return null;
    }

    @Override
    public NodeUserDetails loadUserByUsername(String username) {
        throw new IllegalStateException("We should not be loading users by username.");
    }

    @Override
    public Optional<NodeUserDetails> loadUserBySocialProvider(String providerId, String providerUserId) {

        User user = userRepo.findByConnectionKey(providerId, providerUserId);

        if (user == null) {
            return Optional.empty();
        }

        return Optional.of(new NodeUserDetails(user));
    }

    @Override
    public User loadUserWithVotes(String stableId) {
        return userRepo.loadUserWithVotes(stableId);
    }

    @Override
    public boolean isPublishedAuthor(String authorStableId) {
        return userRepo.isPublishedAuthor(authorStableId);
    }

    @Override
    public Author changeAuthorName(String authorStableId, String authorName) {
        Author author = userRepo.loadAuthor(authorStableId);
        author.setDisplayName(authorName);
        session.save(author);
        return author;
    }

    @Override
    public NodeUserDetails loadUserByUserId(String stableId) throws UsernameNotFoundException {
        User user = userRepo.getUser(stableId);

        if (user == null) {
            throw new ResourceNotFoundException("No user found with id " + stableId);
        }

        return new NodeUserDetails(user);
    }
}
