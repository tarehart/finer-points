package com.nodestand.service;

import com.nodestand.auth.NodeUserDetails;
import com.nodestand.nodes.User;
import com.nodestand.nodes.repository.UserRepository;
import org.neo4j.ogm.cypher.Filter;
import org.neo4j.ogm.exception.NotFoundException;
import org.neo4j.ogm.session.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.data.neo4j.util.IterableUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mh
 * @since 06.03.11
 */
@Component
public class NodeUserDetailsServiceImpl implements NodeUserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private Session session;

    public User getCurrentUser() {
        return getUserFromSession();
    }

    @Override
    public void setCurrentUser(User user) {
        setUserInSession(user);
    }

    @Override
    public boolean userSignedIn() {
        return getCurrentUser() != null;
    }

    @Override
    public void remove() {
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(null);
    }

    @Override
    public NodeUserDetails loadUserByUsername(String socialId) throws UsernameNotFoundException, DataAccessException {
        final User user = findBySocialId(socialId);
        if (user==null) throw new UsernameNotFoundException("Social ID not found: " + socialId);
        return new NodeUserDetails(user);
    }

    private User findByDisplayName(String displayName) {
        try {
            return IterableUtils.getSingle(session.loadAll(User.class, new Filter("displayName", displayName)));
        } catch (NotFoundException e) {
            return null;
        }
    }

    private User findBySocialId(String socialId) {
        try {
            return IterableUtils.getSingle(session.loadAll(User.class, new Filter("socialId", socialId)));
        } catch (NotFoundException e) {
            return null;
        }

    }

    @Override
    public User getUserFromSession() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof NodeUserDetails) {
            NodeUserDetails userDetails = (NodeUserDetails) principal;
            return userDetails.getUser();
        }
        return null;
    }


    @Override
    @Transactional
    public NodeUserDetails register(String socialId, String login) {
        User found = findByDisplayName(login);
        if (found!=null) throw new RuntimeException("Login already taken: " + login);

        User user = new User(socialId, login, User.Roles.ROLE_USER);
        userRepo.save(user);

        setUserInSession(user);
        return new NodeUserDetails(user);
    }


    void setUserInSession(User user) {
        SecurityContext context = SecurityContextHolder.getContext();
        NodeUserDetails userDetails = new NodeUserDetails(user);
        PreAuthenticatedAuthenticationToken authentication = new PreAuthenticatedAuthenticationToken(userDetails, user.getSocialId());
        context.setAuthentication(authentication);
    }

}
