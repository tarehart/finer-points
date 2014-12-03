package com.nodestand.nodes;

import com.nodestand.service.NodeUserDetails;
import com.nodestand.service.NodeUserDetailsService;
import com.nodestand.service.PasswordEncoderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author mh
 * @since 06.03.11
 */
public class NodeUserDetailImpl implements NodeUserDetailsService {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    PasswordEncoderService passwordEncoderService;

    @Override
    public NodeUserDetails loadUserByUsername(String login) throws UsernameNotFoundException, DataAccessException {
        final User user = findByLogin(login);
        if (user==null) throw new UsernameNotFoundException("Username not found: " + login);
        return new NodeUserDetails(user);
    }

    private User findByLogin(String login) {
        return userRepo.findBySchemaPropertyValue("login", login);
    }

    @Override
    public User getUserFromSession() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        Object principal = authentication.getPrincipal();
        if (principal instanceof NodeUserDetails) {
            NodeUserDetails userDetails = (NodeUserDetails) principal;
            return userDetails.getUser();
        }
        return null;
    }


    @Override
    @Transactional
    public User register(String login, String name, String password) {
        User found = findByLogin(login);
        if (found!=null) throw new RuntimeException("Login already taken: " + login);
        if (name==null || name.isEmpty()) throw new RuntimeException("No name provided.");
        if (password==null || password.isEmpty()) throw new RuntimeException("No password provided.");

        User user = new User(login, name, password, passwordEncoderService, User.Roles.ROLE_USER);
        userRepo.save(user);

        setUserInSession(user);
        return user;
    }

    void setUserInSession(User user) {
        SecurityContext context = SecurityContextHolder.getContext();
        NodeUserDetails userDetails = new NodeUserDetails(user);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails, user.getPassword(),userDetails.getAuthorities());
        context.setAuthentication(authentication);

    }

}
