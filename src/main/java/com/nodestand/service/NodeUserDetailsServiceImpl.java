package com.nodestand.service;

import com.nodestand.nodes.User;
import com.nodestand.nodes.UserRepository;
import com.nodestand.auth.NodeUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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

    private static final ThreadLocal<User> currentUser = new ThreadLocal<User>();

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
        return userRepo.findBySchemaPropertyValue("displayName", displayName);
    }

    private User findBySocialId(String socialId) {
        return userRepo.findBySchemaPropertyValue("socialId", socialId);
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
